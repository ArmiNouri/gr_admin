package controllers

import play.api._
import play.api.mvc._
import models._
import models.excel._

object Application extends Controller {

  var hrFile: record = new record()
  var deptFiles: List[record] = List.empty

  def index = Action {
    hrFile = new record()
    deptFiles = List.empty
    Ok(views.html.uploadHR("Please upload your HR file."))
  }

  def getHR = Action(parse.multipartFormData) { request =>
    val f = request.body.file("HR")
    if (f.size > 0 ){
      hrFile = new record(Some(f.head.filename), Some(File.uploadHR(f)))
      Ok(views.html.uploadDept("Please upload your college files one by one:", List()))
    }
    else {
      BadRequest("File not found!")
    }
  }

  def getDept = Action(parse.multipartFormData) { request =>
    val f = request.body.file("Dept")
    if (f.size > 0 ){
      deptFiles = deptFiles :+ new record(Some(f.head.filename), Some(File.uploadDept(f)))
      println(deptFiles)
      Ok(views.html.uploadDept("Please upload your college files one by one:", deptFiles.map(_.filename.getOrElse("???"))))
    }
    else {
      BadRequest("File not found!")
    }
  }

  def deleteDept(f: String) = Action {
    deptFiles = deptFiles.filter(_.filename == f)
    Ok(views.html.uploadDept("Please upload your college files one by one:", deptFiles.map(_.filename.getOrElse("???"))))
  }

  def prcs = Action {
    if(deptFiles.size == 0){
      Ok(views.html.uploadDept("You did not add any college files. Please upload your files one by one:", deptFiles.map(_.filename.getOrElse("???"))))
    } else {
      val errors = (for { d <- deptFiles } yield File.compare(File.prcsDept(d.workbook, d.filename), File.prcsHR(hrFile.workbook), d.filename.getOrElse("???"))).toList.flatten
      File.prcsOutput(hrFile.workbook, errors, hrFile.filename.getOrElse("???"))
      Ok(views.html.results("Thank you. Here is the list of errors found across the files. You can find an annotated copy of the HR file in the parent folder of this app.", errors))
    }
  }
}