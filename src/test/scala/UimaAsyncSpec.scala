import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.cas.SerialFormat
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import scala.xml.XML

class UimaAsyncSpec extends FunSpec with Matchers with PropertyChecks {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  def withBroker[T](block: => T): T = {
    UimaAsync.start()
    try block
    finally UimaAsync.stop()
  }
    
  describe("UimaAppContext") {
    it("should have sensible defaults") {
      val appCtx = UimaAppContext()
      appCtx.dd2SpringXsltFilePath should not be empty
      appCtx.saxonClasspath should not be empty
    }

    val appCtxs = Gen.resultOf(UimaAppContext)
    it("should return its settings as a java.util.Map") {
      forAll(appCtxs) { appCtx =>
        val map = appCtx.toMap
        map.get(UimaAsynchronousEngine.DD2SpringXsltFilePath) shouldBe appCtx.dd2SpringXsltFilePath
        map.get(UimaAsynchronousEngine.SaxonClasspath) shouldBe appCtx.saxonClasspath
        map.get(UimaAsynchronousEngine.ServerUri) shouldBe appCtx.serverUri
        map.get(UimaAsynchronousEngine.ENDPOINT) shouldBe appCtx.endpoint
        map.get(UimaAsynchronousEngine.CasPoolSize) shouldBe appCtx.casPoolSize
        map.get(org.apache.uima.UIMAFramework.CAS_INITIAL_HEAP_SIZE) shouldBe appCtx.casInitialHeapSize.toString
        map.get(UimaAsynchronousEngine.ApplicationName) shouldBe appCtx.applicationName
        map.get(UimaAsynchronousEngine.GetMetaTimeout) shouldBe appCtx.getMetaTimeout
        map.get(UimaAsynchronousEngine.SERIALIZATION_STRATEGY) shouldBe appCtx.serializationStrategy.name()
        appCtx.timeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.Timeout) shouldBe t
          case None => map should not (contain key UimaAsynchronousEngine.Timeout)
        }
        appCtx.cpcTimeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.CpcTimeout) shouldBe t
          case None => map should not (contain key UimaAsynchronousEngine.CpcTimeout)
        }
      }
    }
  }

  describe("UimaAsyncDeploymentConfig") {
    it("should generate an XML file with default config") {
      val appCtx = UimaAppContext()
      val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
      val fname = config.toXML()
      val xml = XML.loadFile(fname)
      
      val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment" 
      (deploy \\ "casPool" \@ "numberOfCASes") shouldBe "1"
      (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint

      val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
      new java.io.File(engineXML) shouldBe 'exists
    }

    val appCtxs = for {
      dd2SpringXsltFilePath <- Gen.alphaStr
      saxonClasspath <- Gen.alphaStr
      serverUri <- Gen.alphaStr
      endpoint <- Gen.alphaStr
      casPoolSize <- Gen.posNum[Int]
      casInitialHeapSize <- Gen.posNum[Int]
      applicationName <- Gen.alphaStr
      getMetaTimeout <- Gen.posNum[Int]
      timeout <- Gen.option(Gen.posNum[Int])
      cpcTimeout <- Gen.option(Gen.posNum[Int])
      serializationStrategy <- Gen.oneOf(SerializationStrategy.values())    
    } yield UimaAppContext(dd2SpringXsltFilePath = dd2SpringXsltFilePath,
        saxonClasspath = saxonClasspath,
        serverUri = serverUri,
        endpoint = endpoint,
        casPoolSize = casPoolSize,
        casInitialHeapSize = casInitialHeapSize,
        applicationName = applicationName,
        getMetaTimeout = getMetaTimeout,
        timeout = timeout,
        cpcTimeout = cpcTimeout,
        serializationStrategy = serializationStrategy)
     
    it("should generate an XML file with specified config") {
      forAll(appCtxs) { appCtx =>
        val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
        val fname = config.toXML()
        val xml = XML.loadFile(fname)    
        val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment" 
        (deploy \\ "casPool" \@ "numberOfCASes") shouldBe appCtx.casPoolSize.toString
        (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint
        val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
        new java.io.File(engineXML) shouldBe 'exists
      }
    }
  }
  
  describe("UimaAsync") {
    describe("when constructed") {
      it("should create the UimaAsynchronousEngine but not initialize it") {
        val uimaAsync = new UimaAsync()
        uimaAsync.engine.getMetaData shouldBe null
      }
    }
    describe("on calling `start(corpus, process)`") {
      it("should process the corpus, returning results in an iterator") {
        
      }
    }

  }
}
