package controllers

import play.api._
import play.api.mvc._
import org.apache.poi.ss.usermodel.{Font, IndexedColors}


import models._
import models.excel._

object File extends Controller {

  def uploadHR(f: Option[play.api.mvc.MultipartFormData.FilePart[play.api.libs.Files.TemporaryFile]]): FancyWorkbook = {
    import Implicits._

    val input = f.head.ref.file.getAbsoluteFile()
    println(f.head.filename)
    val w = FancyWorkbook.createFromFile(input)

    w
  }

  def uploadDept(f: Option[play.api.mvc.MultipartFormData.FilePart[play.api.libs.Files.TemporaryFile]]): FancyWorkbook = {
    import Implicits._

    val input = f.head.ref.file.getAbsoluteFile()
    println(f.head.filename)
    val w = FancyWorkbook.createFromFile(input)

    w
  }

 def checkHeaders(sheet: FancySheet, fs: List[cl]): cl = {
   import Implicits._
   var check = new cl("00", "")
   val loop = for ( i <- fs ) {
     if (!str.equal(sheet.cell(i.address).value, i.value)) {
        check = new cl(i.address, i.value)
     }
   }
   check
 }

  def prcsHR(w: Option[FancyWorkbook]): List[person]  = w match {
    case None => throw new Exception ("No HR file found. Please make sure you're uploaded the right file.")
    case Some(w) => {
      import Implicits._
      val sheet = w.sheetAt(0)
      val check = checkHeaders(sheet, HR.fields)
      if(check.address != "00") throw new Exception ("In the HR file, the column header at " + check.address + " does not match '" + check.value + "'.")
      else {
        val lastRow = sheet.lastRowIndex
        val people =
          for {
            i <- 1 to lastRow
          } yield { val name = sheet.cellAt(3, i).stringValue.split(",")
                    new person(
                             i+1,
                             try { sheet.cellAt(0, i).stringValue } catch { case e: Throwable => throw new Exception("No string value at HR file cell: " + sheet.cellAt(0, i).addr) },
                             try { sheet.cellAt(1, i).stringValue } catch { case e: Throwable => throw new Exception("No string value at HR file cell: " + sheet.cellAt(1, i).addr) },
                             new Name( name(1), name(0) ),
                             try { sheet.cellAt(7, i).numericValue.toDouble } catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(7, i).addr) },
                             try { Some(sheet.cellAt(8, i).numericValue.toDouble) } catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(8, i).addr) },
                             try { sheet.cellAt(9, i).numericValue.toDouble } catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(9, i).addr) },
                             try { sheet.cellAt(12, i).stringValue}  catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(12, i).addr) },
                             try { sheet.cellAt(13, i).numericValue.toDouble } catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(13, i).addr) },
                             try { sheet.cellAt(17, i).numericValue.toDouble} catch { case e: Throwable => throw new Exception("No numeric value at HR file cell: " + sheet.cellAt(17, i).addr) }
                    )}

        people.toList
      }
    }
  }

  def prcsDept(w: Option[FancyWorkbook], n: Option[String]): List[employee]  = w match {
    case None => throw new Exception ("The department file named " + n + " is missing. Please make sure you're uploaded the right file.")
    case Some(w) => {
      import Implicits._
      val sheet = w.sheetAt(0)
      val check = checkHeaders(sheet, Dept.fields)
      if(check.address != "00") throw new Exception ("In the departmental file named " + n + ", the column header at " + check.address + " does not match '" + check.value + "'.")
      else {
        val lastRow = sheet.lastRowIndex
        val employees =
          for {
            i <- 1 to lastRow
          } yield {
            new employee(
              i+1,
              try { sheet.cellAt(0, i).stringValue } catch { case e: Throwable => throw new Exception("No string value at college file cell: " + sheet.cellAt(0, i).addr) },
              try { new Name(sheet.cellAt(1, i).stringValue, sheet.cellAt(2, i).stringValue)  } catch { case e: Throwable => throw new Exception("No string value at college file cell: " + sheet.cellAt(2, i).addr) },
              try { sheet.cellAt(5, i).stringValue } catch { case e: Throwable => throw new Exception("No string value at college file cell: " + sheet.cellAt(5, i).addr) },
              try { sheet.cellAt(7, i).stringValue  } catch { case e: Throwable => throw new Exception("No string value at college file cell: " + sheet.cellAt(7, i).addr) },
              try { sheet.cellAt(8, i).numericValue.toDouble  } catch { case e: Throwable => throw new Exception("No numeric value at college file cell: " + sheet.cellAt(8, i).addr) },
              try { sheet.cellAt(9, i).stringValue  } catch { case e: Throwable => throw new Exception("No string value at college file cell: " + sheet.cellAt(9, i).addr) }
                ) }

        employees.toList
      }
    }
  }

  def compare(employees: List[employee], people: List[person], filename: String): List[error] = {
    var errors: List[error] = List.empty
    if(employees.size == 0) throw new Exception("College file titled " + filename + " is empty.")
    if(people.size == 0) throw new Exception("HR file is empty.")
    for (e <- employees) {
      val p1 = people.filter(_.name == e.name)
      if(p1.size == 0) errors = errors :+ new error(0, filename, e.name.toString, "None", "Row: " + e.address, "Employee named " + e.name + " is listed on the college file titled '" + filename + "', but cannot be found on the HR file.")
      else {
        val p2 = p1.filter(x => x.college == e.college && mappings.job_to_position(x.job) == e.position)

        if(p2.size == 0) { val pos = try { mappings.job_to_position.map(_.swap).apply(e.position) } catch { case x: Throwable => e.position  + "? (unknown position) " }; errors = errors :+ new error(0, filename, e.name.toString, "None", "Row: " + e.address, "Employee named " + e.name + " is listed on the college file titled '" + filename + "' as a " + pos + ", but on the HR file, s/he is not listed under the same department with the same job.") }

        else for (p <- p2.distinct) {
          val p = p2.head
          if(str.equal(e.status, "rehire") && (p.start_date >= mappings.date || p.rehire_date.getOrElse(0d) == 0d)) errors = errors :+ new error(p.address, filename, p.name.toString, "H" + p.address, "F" + e.address, "Employee is listed as 'rehired' on the college file, but has a start date later than or equal to 9/1/2013 on the HR file, or doesn't have a rehire date on the HR file.")
          if(str.equal(e.status, "hire") && (p.start_date < mappings.date || p.rehire_date.getOrElse(0d) > 0d)) errors = errors :+ new error(p.address, filename, p.name.toString, "H" + p.address, "F" + e.address, "Employee is listed as 'new hire' on the college file, but has a start date earlier than 9/1/2013 on the HR file, or has a rehire date on the HR file.")
          if(!str.equal(e.position, mappings.job_to_position(p.job))) errors = errors :+ new error(p.address, filename, p.name.toString, "M" + p.address, "F" + e.address.toString, "Employee's job is listed as " + e.position + " on the college file, but as " + p.job + "on the HR file.")
          if(e.fte != mappings.hours_to_fte(p.hours)) errors = errors :+ new error(p.address, filename, p.name.toString, "N" + p.address, "I" + e.address, "Employee's fte is listed as " + e.fte + " on the college file but the employee's weekly hours are listed as " + p.hours + " on the HR file.")
          if(!str.equal(e.period, "ay") && p.end_date > 41650.00) errors = errors :+ new error(p.address, filename, p.name.toString, "J" + p.address, "J" + e.address, "Employee's appointment period is not yearly in the college file. But the expected end date on the HR file exceeds the end of the semester.")
        }
      }
    }
    val pcol = people.filter(x => x.college == employees.head.college)
    for(p <- pcol){
      if (employees.filter(_.name == p.name).size == 0) errors = errors :+ new error(p.address, filename, p.name.toString, "Row: " + p.address, "None", "Employee is listed on the HR file under the college of " + p.college + " but cannot be found on the college file titled " + filename + ".")
      val rate = try {mappings.hours_to_rate(p.hours)}catch{case x:Throwable => -1}
      if (p.rate != rate) errors = errors :+ new error(p.address, filename, p.name.toString, "R" + p.address, "None", "Employee's comp rate is listed as " + (if(rate<0) "an unknown value." else p.rate + " but the same employee's weekly hours are listed as " + p.hours + " on the HR file."))
      if (pcol.filter(_ == p).size > 1) errors = errors :+ new error(p.address, filename, p.name.toString, "Row: " + p.address, "None", "Employee is listed multiple times under the college of " + p.college + " under the same job.")
    }
    errors
  }

  def prcsOutput(w: Option[FancyWorkbook], errors: List[error], filename: String) = w match {
    case None => throw new Exception ("No HR workbook found.")
    case Some(w) => {
      import Implicits._
      val sheet:FancySheet = w.sheetAt(0)
      val lr = sheet.lastRowIndex
      println(lr)

      val output = FancyWorkbook.createXls
      val os = output.sheet("errors")

      for (r <- 0 until lr) {
        val lc = sheet.rowAt(r).lastColIndex
        for ( c <- 0 until lc ) {
          val v = sheet.cellAt(c, r)
          try { os.cellAt(c, r).value(v.value) } catch {
            case e: Throwable => os.cellAt(c, r).value(v.numericValue)
          }
          val e = errors.filter( x => x.whereHR == sheet.cellAt(c, r).addr || x.order == r + 1)
          if(e.size > 0)  {

            val boldRed14Font = (f: Font) => {
              f.setBoldweight(Font.BOLDWEIGHT_BOLD)
              f.setColor(IndexedColors.RED.getIndex)
              f.setFontHeightInPoints(14)
            }

            os.cellAt( c , r ).replaceFont(boldRed14Font)
            os.cell("AG" + (r + 1)).value(e.map( x => x.what).distinct.mkString(" - "))
          }
        }
      }
      val notFound = errors.filter(_.whereHR == "None")
      val ss = output.sheet("people_not_found")
      for( i <- 1 to notFound.size) {
          ss.cell("A" + i).value(notFound(i-1).what)
      }
      output.write(new java.io.File("../" + filename + "_output.xls"))
    }
  }
}