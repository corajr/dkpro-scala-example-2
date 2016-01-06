import org.scalatest._
import de.tudarmstadt.ukp.dkpro.core.api.metadata.`type`.DocumentMetaData
import de.tudarmstadt.ukp.dkpro.core.api.ner.`type`.NamedEntity
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.`type`.Lemma

class ProcessSpec extends FunSpec with Matchers {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  describe("Process") {
    describe("lemmatize") {
      describe("when passed a Corpus") {
        it("should lemmatize the corpus") {
          import Process.EnrichedJCas

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
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)
          val jcasIterator = Process.lemmatize(corpus)
          val jcas = jcasIterator.next()
          val lemmas = jcas.select(classOf[Lemma]).take(5).map(_.getValue).toSeq
          lemmas shouldBe Seq("fellow", "-", "citizen", "of", "the")
        }
      }
    }
    
    describe("lemmatizeAndNER") {
      describe("when passed a corpus") {
        it("should return the expected lemmas and named entities from the corpus") {
          import Process.EnrichedJCas
          
          val corpus = Corpus.fromDir(corpusDir)
          val jcasIterator = Process.lemmatizeAndNER(corpus)
          jcasIterator
            .find(_.selectSingle(classOf[DocumentMetaData]).getDocumentTitle == "1949-Truman.txt")
            .fold(fail("No JCas matched")) { jcas =>
              val entities = jcas.select(classOf[NamedEntity]).take(5).map(_.getCoveredText).toVector
              val lemmas = jcas.select(classOf[Lemma]).take(5).map(_.getValue).toVector
              entities shouldBe Vector("Communism", "Communism", "Democracy", "Communism", "Communism")
              lemmas shouldBe Vector("mr.", "vice", "president", ",", "mr.")
            }
        }
      }
    }
  }
}