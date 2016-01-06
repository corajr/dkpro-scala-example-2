import org.scalatest._

class CorpusSpec extends FunSpec with Matchers {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  describe("Corpus") {
    describe("fromDir") {
      it("should produce a Corpus with a CollectionReaderDescription from a directory path") {
        val corpus = Corpus.fromDir(corpusDir)
        val params = corpus.reader.getCollectionReaderMetaData().getConfigurationParameterSettings()
        val patterns = params.getParameterValue("patterns")
        // retrieve all .txt documents
        patterns shouldBe Array("[+]**/*.txt")
      }
    }
  }
}
