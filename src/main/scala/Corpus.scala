import org.apache.uima.collection.CollectionReaderDescription
import org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader

case class Corpus(reader: CollectionReaderDescription)

object Corpus {
  def fromDir(directory: String, pattern: String = "[+]**/*.txt", lang: String = "en"): Corpus =
    Corpus(createReaderDescription(
      classOf[TextReader],
      ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, directory,
      ResourceCollectionReaderBase.PARAM_PATTERNS, pattern,
      ResourceCollectionReaderBase.PARAM_LANGUAGE, lang))
}
