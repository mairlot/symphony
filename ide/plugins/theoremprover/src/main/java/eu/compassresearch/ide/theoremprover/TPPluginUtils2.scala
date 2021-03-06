package eu.compassresearch.ide.theoremprover
import isabelle.eclipse.core.IsabelleCore
import isabelle.eclipse.core.app.Isabelle
import eu.compassresearch.core.analysis.theoremprover.IsabelleTheory
import eu.compassresearch.ide.core.resources.ICmlModel
import eu.compassresearch.ide.theoremprover.TPListener

object TPPluginUtils2 {
  
  def addThyToListener(ithy : IsabelleTheory, tpListener : TPListener, model : ICmlModel) {
    tpListener.registerNode(ithy.thyNode, model);
  }
  
  def checkHOLVDM(): Either[String, Isabelle] = {
    val isabelle = IsabelleCore.isabelle

    // val test1 = Document.Node.Name("Test", "/home/simon/Isabelle", "Test"); 
    
    // FIXME: This should also check that the right session is loaded
    isabelle.session match {
      case Some(s) => {
        val ithy = new IsabelleTheory(s, "Test", "/home/simon/Isabelle");

        ithy.init();
        
        val snapshot1 = s.snapshot();
        
        ithy.addThm(new ithy.IsabelleTheorem("simpleLemma", "True", "by simp"))
        
        val snapshot2 = s.snapshot();
        val snapshot3 = s.snapshot();
        
        return Right(isabelle);
        
        
        
        /*
        val thy = Source.fromFile("/home/simon/Isabelle/Test.thy").mkString
        val header = s.thy_load.check_thy_text(test1, thy)
        // val perspective = Text.Perspective(List(Text.Range(0,thy.length())))
        
        val cmd1 = "theory Test imports Main begin \n\n"
        
        s.update(List( s.header_edit(test1, header)
                     , test1 -> Document.Node.Clear()
                     , test1 -> Document.Node.Edits(List(Text.Edit.insert(0, cmd1)))
                     , test1 -> Document.Node.Perspective(Text.Perspective(List(Text.Range(0,cmd1.length()))))));

        val snapshot1 = s.snapshot();
        val cmd2 = "lemma test1: \"True\""
          
        s.update(List(test1 -> Document.Node.Edits(List(Text.Edit.insert(cmd1.length(),cmd2)))
                     ,test1 -> Document.Node.Perspective(Text.Perspective(List(Text.Range(0, cmd1.length() + cmd2.length()))))));
        
        val snapshot2 = s.snapshot();
        
        val cmd3 = "apply(simp)"
          
        s.update(List(test1 -> Document.Node.Edits(List(Text.Edit.insert(cmd1.length() + cmd2.length(),cmd3)))
                     ,test1 -> Document.Node.Perspective(Text.Perspective(List(Text.Range(0, cmd1.length() + cmd2.length() + cmd3.length()))))));
        
        val snapshot3 = s.snapshot();

        val cmd4 = "done"
        
        s.update(List(test1 -> Document.Node.Edits(List(Text.Edit.insert(cmd1.length() + cmd2.length() + cmd3.length(), cmd4)))
                     ,test1 -> Document.Node.Perspective(Text.Perspective(List(Text.Range(0, cmd1.length() + cmd2.length() + cmd3.length() + cmd4.length()))))));
        
        val snapshot4 = s.snapshot();
        
        
        val test1Cmd = ithy.thmCmd("test1");
        val test1Prf = ithy.proofCmds("test1");
        */
        
        
      }
      case None => {
        return Left("Isabelle is not started - please load HOL-UTP-VDM")
      }
    }
    
  }
  
}