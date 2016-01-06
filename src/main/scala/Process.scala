import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline
import org.apache.uima.fit.util.JCasUtil
import org.apache.uima.jcas.JCas
import org.apache.uima.jcas.cas.TOP
import de.tudarmstadt.ukp.dkpro.core.clearnlp.{ClearNlpSegmenter, ClearNlpLemmatizer, ClearNlpPosTagger}
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder

case class Process(engines: Seq[AnalysisEngineDescription]) {
  import scala.collection.JavaConversions._
  
  def apply(corpus: Corpus) = runSingleThread(corpus)
  
  def runSingleThread(corpus: Corpus): Iterator[JCas] =
    iteratePipeline(
      corpus.reader,
      engines: _*).iterator()
  
  def runMultiThread(corpus: Corpus): Iterator[JCas] =
    runSingleThread(corpus)
}

object Process {
  val lemmatize =
    Process(
      createEngineDescription(classOf[ClearNlpSegmenter]),
      createEngineDescription(classOf[ClearNlpPosTagger]), 
      createEngineDescription(classOf[ClearNlpLemmatizer]) 
    )

  val lemmatizeAndNER =
    Process(
      createEngineDescription(classOf[ClearNlpSegmenter]),
      createEngineDescription(classOf[ClearNlpPosTagger]), 
      createEngineDescription(classOf[ClearNlpLemmatizer]),
      createEngineDescription(classOf[OpenNlpNameFinder]) 
    )    
    
  def apply(engines: AnalysisEngineDescription*)(implicit d: DummyImplicit) = new Process(Seq(engines: _*))
  
  implicit class EnrichedJCas(jcas: JCas) {
    import scala.collection.JavaConversions._

    def select[T <: TOP](clazz: Class[T]): Iterator[T] = JCasUtil.select(jcas, clazz).iterator()
    def selectSingle[T <: TOP](clazz: Class[T]): T = JCasUtil.selectSingle(jcas, clazz)
  }
}