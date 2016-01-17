import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline
import org.apache.uima.fit.util.JCasUtil
import org.apache.uima.jcas.JCas
import org.apache.uima.jcas.cas.TOP
import de.tudarmstadt.ukp.dkpro.core.clearnlp.{ ClearNlpSegmenter, ClearNlpLemmatizer, ClearNlpPosTagger }
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.`type`.Lemma
import de.tudarmstadt.ukp.dkpro.core.api.metadata.`type`.DocumentMetaData

import uimaAS.Util

case class Process(engines: Seq[AnalysisEngineDescription]) {
  import scala.collection.JavaConversions._

  def apply[T](corpus: Corpus)(block: JCas => T): Util.Results[T] =
    runSingleThread(corpus)(block)

  def runSingleThread[T](corpus: Corpus)(block: JCas => T): Util.Results[T] =
    iteratePipeline(
      corpus.reader,
      engines: _*).iterator().map(Process.wrapBlock(block)).toMap

  def runMultiThread[T](corpus: Corpus)(block: JCas => T): Util.Results[T] = {
    val uimaAS = new UimaAsync()
    val futureResults = uimaAS.start(corpus, this, Process.wrapBlock(block))
    Await.result(futureResults, Duration.Inf)
  }
}

object Process {
  val lemmatize =
    Process(
      createEngineDescription(classOf[ClearNlpSegmenter]),
      createEngineDescription(classOf[ClearNlpPosTagger]),
      createEngineDescription(classOf[ClearNlpLemmatizer]))

  val lemmatizeAndNER =
    Process(
      createEngineDescription(classOf[ClearNlpSegmenter]),
      createEngineDescription(classOf[ClearNlpPosTagger]),
      createEngineDescription(classOf[ClearNlpLemmatizer]),
      createEngineDescription(classOf[OpenNlpNameFinder]))

  def apply(engines: AnalysisEngineDescription*)(implicit d: DummyImplicit) = new Process(Seq(engines: _*))

  def wrapBlock[T](block: JCas => T): Util.Block[T] = {
    { jcas =>
      val metadata = jcas.selectSingle(classOf[DocumentMetaData])
      val title = metadata.getDocumentTitle()
      title -> block(jcas)
    }
  }

  implicit class EnrichedJCas(jcas: JCas) {
    import scala.collection.JavaConversions._

    def select[T <: TOP](clazz: Class[T]): Iterator[T] = JCasUtil.select(jcas, clazz).iterator()
    def selectSingle[T <: TOP](clazz: Class[T]): T = JCasUtil.selectSingle(jcas, clazz)
  }
}
