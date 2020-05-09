
import coursier.maven.MavenRepository
import mill._
import mill.scalajslib._
import mill.scalalib._

object todo extends ScalaJSModule {

  def scalaVersion = "2.13.1"

  def scalaJSVersion = "1.0.1"

  def ivyDeps = Agg(
    ivy"com.lihaoyi::scalatags::0.8.6",
    ivy"de.tuda.stg::rescala::0.30.0"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://dl.bintray.com/stg-tud/maven")
  )
}