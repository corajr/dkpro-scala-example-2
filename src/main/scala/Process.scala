import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline
import org.apache.uima.jcas.JCas
import de.tudarmstadt.ukp.dkpro.core.clearnlp.{ClearNlpSegmenter, ClearNlpLemmatizer, ClearNlpPosTagger}


case class Process(engines: Seq[AnalysisEngineDescription]) {
  import scala.collection.JavaConversions._
  
  def apply(corpus: Corpus): Iterator[JCas] =
    iteratePipeline(
      corpus.reader,
      engines: _*).iterator()
}

object Process {
  val lemmatize =
    Process(
      createEngineDescription(classOf[ClearNlpSegmenter]),
      createEngineDescription(classOf[ClearNlpPosTagger]), 
      createEngineDescription(classOf[ClearNlpLemmatizer]) 
    )

  def apply(engines: AnalysisEngineDescription*)(implicit d: DummyImplicit) = new Process(Seq(engines: _*))
}