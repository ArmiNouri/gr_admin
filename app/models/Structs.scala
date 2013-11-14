package models

import excel._

/**
 * Using excel library by: ishiiyoshinori
 * Date: 11/05/04
 */

class record (
  val filename: Option[String] = None,
  val workbook: Option[FancyWorkbook] = None
               ) {
  override def toString = "[ filename: " + filename.getOrElse("None") + ", workbook: " + workbook.getOrElse("None") + " ]"
}

class error (
  val order: Int,
  val whichDept: String,
  val whichPerson: String,
  val whereHR: String,
  val whereDept: String,
  val what: String
              ) {
  override def toString = "[ which dept: " + whichDept + ", where in HR: " + whereHR + ", where in Dept: " + whereDept + ", what: " + what + " ]"
  def ==(that: error): Boolean = {
    if (this.whichDept == that.whichDept && this.whichPerson == that.whichPerson && this.whereHR == that.whereHR && this.whereDept == that.whichDept && this.what == that.what) true
    else false
  }
}

class Name (
  val first: String,
  val last: String
             ) {
  def ==(that: Name): Boolean = {
    val thiss = str.abbreviate(this.toString.toLowerCase().replace(".", " ").replace("-", " ")).split(" ")
    val thatt = str.abbreviate(that.toString.toLowerCase().replace(".", " ").replace("-", " ")).split(" ")
    if( thiss.intersect(thatt).size > 1 ) true
    else false
  }
  override def toString: String = first + " " + last
}

class person (
  val address: Int,
  val college: String,
  val dept: String,
  val name: Name,
  val start_date: Double,
  val rehire_date: Option[Double] = Some(0),
  val end_date: Double,
  val job: String,
  val hours: Double,
  val rate: Double
               )
{
  val mapper: Map[Name, person] = Map(this.name -> this)

  def ==(that: person): Boolean = {
    if(this.college == that.college && this.dept == that.dept && this.name.toString == that.name.toString && this.job == that.job) true
    else false
  }
}

class employee (
  val address: Int,
  val college: String,
  val name: Name,
  val status: String,
  val position: String,
  val fte: Double,
  val period: String
                 )
{
  val mapper: Map[Name, employee] = Map(this.name -> this)

  def ==(that: employee): Boolean = {
    if(this.college == that.college && this.name.toString == that.name.toString && this.position == that.position) true
    else false
  }
}
class cl (
  val address: String,
  val value: String
           )

object HR {
  val fields = List(
    new cl("A1", "MBU/College"),
    new cl("B1", "Department"),
    new cl("D1", "Name"),
    new cl("H1", "Start Date"),
    new cl("I1", "Rehire Dt"),
    new cl("J1", "Expected End Date"),
    new cl("M1", "Job Title"),
    new cl("N1", "Std Hrs/Wk"),
    new cl("R1", "Comp Rate"))
}

object Dept {
  val fields = List(
    new cl("A1", "Department"),
    new cl("B1", "First Name"),
    new cl("C1", "Last Name"),
    new cl("F1", "New Hire/Rehire"),
    new cl("H1", "Position"),
    new cl("I1", "FTE"),
    new cl("J1", "Appointment Period"))
}

object str {
  def strip(a: String): String = a.filter(_ != ' ').filter(_ != '/').filter(_ != '.').filter(_ != ',')
  def escape(a: String): String = a.replace("date", "dt").replace("department", "dept")
  def equal(a: String, b: String): Boolean = {
    if(strip(escape((a.toLowerCase))) == strip(escape((b.toLowerCase)))) true
    else false
  }

  val abbr_map: Map[String, String] = Map (
                  "margaret" -> " maggie meg mary",
                  "megan" -> " meg",
                  "meghan" -> "megan meg",
                  "john" -> " jon",
                  "jonathan" -> " jon john",
                  "james" -> " jim",
                  "alexander" -> " alex ali al",
                  "alexandra" -> " alex ali",
                  "alexandria" -> " alex ali",
                  "alexis" -> " alex ali",
                  "robert" -> " bob rob",
                  "patrick" -> " pat" ,
                  "aleks" -> " alex ali al",
                  "aleksander" -> " alex ali al",
                  "aleksandra" -> " alex ali",
                  "aleksis" -> " alex ali",
                  "catherine" -> " kathy cathy cathie cat kat katie",
                  "catherin" -> " kathy cathy cathie cat kat katie",
                  "katherine" -> " kathy cathy cathie cat kat katie",
                  "katherin" -> " kathy cathy cathie cat kat katie",
                  "kathryn" -> " kathy cathy cathie cat kat katie",
                  "ekatrina" -> " katrin kat katie katy",
                  "elizabeth" -> " liz beth eliza elize",
                  "bethanny" -> " beth",
                  "bethenny" -> " beth",
                  "kimberly" -> " kim",
                  "rachel" -> " rach")

  def abbreviate(s: String): String = {
    s + " " + (for( x <- abbr_map if s contains x._1) yield x._2).mkString(" ")
  }
}

object mappings {
  val date = 41518.00

  val job_to_position: Map[String, String] = Map(
              "Graduate Research Assistant" -> "RA",
              "Graduate Research Assistant I" -> "RAI",
              "Graduate Research Assistant II" -> "RAII",
              "Graduate Teaching Assistant" -> "TA",
              "Graduate Teaching Assistant I" -> "TAI",
              "Graduate Teaching Assistant II" -> "TAII",
              "Graduate Administrative Assist" -> "AA",
              "Graduate Administrative Assist I" -> "AAI",
              "Graduate Administrative ASsist II" -> "AAII",
              "Graduate Assistant" -> "GA")
  val hours_to_fte: Map[Double, Double] = Map(
              18d -> 1.00,
              13.5 -> 0.75,
              9d -> 0.50,
              4.5 -> 0.25)
  val hours_to_rate: Map[Double, Double] = Map(
              18d -> 837.26,
              13.5 -> 627.95,
              9d -> 418.63,
              4.5 -> 209.32)

  val rows = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
}