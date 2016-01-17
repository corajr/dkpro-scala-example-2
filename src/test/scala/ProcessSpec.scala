import org.scalatest._
import de.tudarmstadt.ukp.dkpro.core.api.metadata.`type`.DocumentMetaData
import de.tudarmstadt.ukp.dkpro.core.api.ner.`type`.NamedEntity
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.`type`.Lemma

class ProcessSpec extends FunSpec with Matchers with BrokerFixture {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  def time[R](block: => R): Long = {
      val t0 = System.nanoTime()
      block
      val t1 = System.nanoTime()
      ((t1 - t0) / 1e6).toLong  // ms
  }

  describe("Process") {
    describe("lemmatize") {
      describe("when passed a Corpus") {
        it("should lemmatize the corpus") {
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)
          val lemmaMap = (Process.lemmatize(corpus) { jcas =>
            val lemmas = jcas.select(classOf[Lemma])
            lemmas.size
          })
          lemmaMap should have size 56
          all (lemmaMap.values) should be > 0
        }

        it("should return the expected lemmas from part of the first document") {
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)
          val allLemmas = (Process.lemmatize(corpus) { jcas =>
            jcas.select(classOf[Lemma]).take(5).map(_.getValue).toVector
          })

          allLemmas("1789-Washington.txt") shouldBe Vector("fellow", "-", "citizen", "of", "the")
        }

        it("should be able to run multithreaded") {
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)
          val allLemmas = Process.lemmatize.runMultiThread(corpus) { jcas =>
            jcas.select(classOf[Lemma]).take(5).map(_.getValue).toVector
          }
          allLemmas("1789-Washington.txt") shouldBe Vector("fellow", "-", "citizen", "of", "the")
        }
      }
    }

    describe("lemmatizeAndNER") {
      describe("when passed a corpus") {
        it("should return the expected lemmas and named entities from the corpus") {
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)
          val results = Process.lemmatizeAndNER(corpus) { jcas =>
            if (jcas.selectSingle(classOf[DocumentMetaData]).getDocumentTitle == "1949-Truman.txt") {
              val entities = jcas.select(classOf[NamedEntity]).take(5).map(_.getCoveredText).toVector
              val lemmas = jcas.select(classOf[Lemma]).take(5).map(_.getValue).toVector
              Some((entities, lemmas))
            } else {
              None
            }
          }
          val res = results.values.find(_.nonEmpty).flatten
          res.fold(fail("No JCas matched")) { case (entities, lemmas) =>
            entities shouldBe Vector("Communism", "Communism", "Democracy", "Communism", "Communism")
            lemmas shouldBe Vector("mr.", "vice", "president", ",", "mr.")
          }
        }

        it("should run faster when multithreaded") {
          import Process.EnrichedJCas

          val corpus = Corpus.fromDir(corpusDir)

          val singleThreadMillis = time {
            val singleIterator = Process.lemmatizeAndNER.runSingleThread(corpus)(_ => Unit)
          }

          val multiThreadMillis = time {
            val multiThreadIterator = Process.lemmatizeAndNER.runMultiThread(corpus)(_ => Unit)
          }

          multiThreadMillis should be < singleThreadMillis - 10000
        }
      }
    }
  }
}
