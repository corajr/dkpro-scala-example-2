package uimaAS

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

class DummyAE extends JCasAnnotator_ImplBase {
  override def process(jcas: JCas): Unit = {}
}
