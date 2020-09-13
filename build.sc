
import coursier.maven.MavenRepository
import mill._
import mill.scalajslib._
import mill.scalalib._

object todo extends ScalaJSModule {

  def scalaVersion = "2.13.1"

  def scalaJSVersion = "1.0.1"

  def ivyDeps = Agg(
    ivy"de.tuda.stg::rescala::0.30.0",
    ivy"com.lihaoyi::scalatags::0.8.6",
    ivy"com.lihaoyi::upickle::1.2.0",
    ivy"ba.sake::scalajs-router::0.0.5"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://dl.bintray.com/stg-tud/maven")
  )
}
