import org.scalatest._
import de.tudarmstadt.ukp.dkpro.core.api.metadata.`type`.DocumentMetaData
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.`type`.Lemma

class ProcessSpec extends FunSpec with Matchers {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()
  
  describe("Process") {
    import Process._

    describe("lemmatize") {
      describe("when passed a reader") {
        it("should lemmatize a corpus") {
          val corpus = Corpus.fromDir(corpusDir)
          val lemmaMap = (for {
            jcas <- Process.lemmatize(corpus)
            metadata = jcas.selectSingle(classOf[DocumentMetaData])
            title = metadata.getDocumentTitle()
            lemmas = jcas.select(classOf[Lemma])
          } yield title -> lemmas.size).toMap
          lemmaMap should have size 56
          all (lemmaMap.values) should be > 0
        }

        it("should return the expected lemmas from part of the first document") {          
          val corpus = Corpus.fromDir(corpusDir)
          val jcasIterator = Process.lemmatize(corpus)
          val jcas = jcasIterator.next()
          val lemmas = jcas.select(classOf[Lemma]).take(5).map(_.getValue)
          lemmas.toSeq shouldBe Seq("fellow", "-", "citizen", "of", "the")
        }
      }
    }
  }
}