package ir.phsys.ikbins.notifier

import ir.phsys.sitereader.solr.server.SolrServerFactory
import org.apache.logging.log4j.LogManager
import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
 * @author : Пуя Гуссейни
 *         Email : info@pooya-hfp.ir,pooya.husseini@gmail.com
 *         Date: 22.04.15
 *         Time: 18:01
 */
object SolrServer {
  private val logger = LogManager.getLogger(this.getClass)
  val collection: String = "collection1"
}


class SolrServer{

  val client: HttpSolrClient = SolrServerFactory.create(SolrServer.collection)

}