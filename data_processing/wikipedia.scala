// Script that will parse the full xml dump file
// and create new files in a specified folder where
// each file corresponds to an individual article (named by article ID)

// Note: script was adapted from https://github.com/tuxdna/scala-snippets/blob/f48ba83b88df0465ca27b75451e9847bb8579c37/src/main/scala/xml/wikipedia.scala

package xml

import scala.io.Source
import scala.xml.pull._
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.io.FileOutputStream
import scala.xml.XML

object wikipedia extends App {

  // xmlFile corresponds to the xml dump downloaded from wikipedia
  val xmlFile = "wikidata.xml"
  
  // outputLocation corresponds to the folder where output files will be stored
  val outputLocation = new File("output")

  // The following line might need to be changed on a Windows machine
  // val xml = new XMLEventReader(Source.fromFile(xmlFile)("UTF-8")) 
  val xml = new XMLEventReader(Source.fromFile(xmlFile))

  // Search through document
  var insidePage = false
  var buffer = ArrayBuffer[String]()
  for (event <- xml) {
    event match {
      // This identifies the start of a new page, start a buffer holding the current page's text
      case EvElemStart(_, "page", _, _) => {
        insidePage = true
        val tag = "<page>"
        buffer += tag
      }

      // This identifies the end of a new page (so write out the buffer to a file now)
      case EvElemEnd(_, "page") => {
        val tag = "</page>"
        buffer += tag
        insidePage = false

        writePage(buffer)
        buffer.clear
      }
      
      // If the current line is a tag, add that tag to the buffer
      case e @ EvElemStart(_, tag, _, _) => {
        if (insidePage) {
          buffer += ("<" + tag + ">")
        }
      }

      // If the current line is a closing tag, add the closing tag to the buffer
      case e @ EvElemEnd(_, tag) => {
        if (insidePage) {
          buffer += ("</" + tag + ">")
        }
      }

      // If the current line is text, add that text to the buffer
      case EvText(t) => {
        if (insidePage) {
          buffer += (t)
        }
      }

            
      // Ignore
      case _ => 
    }
  }

  // Write output file
  def writePage(buffer: ArrayBuffer[String]) = {
    val s = buffer.mkString
    val x = XML.loadString(s)
    val pageId = (x \ "id")(0).child(0).toString
    val f = new File(outputLocation, pageId + ".xml")
    println("writing to: " + f.getAbsolutePath())
    val out = new FileOutputStream(f)
    out.write(s.getBytes())
    out.close
  }

}

