package ir.phsys.ikbins.notifier

import ir.phsys.sitereader.solr.server.SolrServerFactory
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.Source
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

/**
  * Created by pooya on 1/13/17.
  */
case class Configuration(name: String, filters: List[String], emails: List[String])

object Main {
  def main(args: Array[String]): Unit = {
    val client: HttpSolrClient = SolrServerFactory.create(SolrServer.collection)
    val content = Source.fromFile("/home/pooya/config.json").getLines().mkString("\n")
    println(s"content = $content")
    val config = mutable.Buffer[Configuration]()
    val parseRaw = JSON.parseRaw(content)
    parseRaw.foreach {
      case i: JSONObject => i.obj.foreach {
        case (item, value: JSONObject) =>
          val filters = value.obj("filters").asInstanceOf[JSONArray].list.map(_.asInstanceOf[String])
          val emails = value.obj("emails").asInstanceOf[JSONArray].list.map(_.asInstanceOf[String])
          config += Configuration(item, filters, emails)
      }
    }


    //    val queryString: String = s"persian_content:'افزایش'"
    config.foreach(i =>
      i.filters.foreach(item => {
        val query: SolrQuery = new SolrQuery
        query.setParam("persian_content", "\"" + item + "\"")
        val queryString: String = s"*:*"

        query.setQuery(queryString)
        query.setStart(0)
        val response = client.query(query).getResults
        response.listIterator().asScala.foreach(println)
      }))


    client.close()
  }
}