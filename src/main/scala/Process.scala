import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.jcas.JCas
import de.tudarmstadt.ukp.dkpro.core.clearnlp.{ClearNlpSegmenter, ClearNlpLemmatizer, ClearNlpPosTagger}


case class Process(engines: Seq[AnalysisEngineDescription]) {
  def apply(corpus: Corpus): Iterator[JCas] = ???
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