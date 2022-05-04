package top.viewv;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.util.Collections;

public class SootSetup {
    public static void initSoot(String sourceDirectory, String classname) {
        // Initialize Soot
        G.reset();

        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        /*
        enable the keep line number so the unit will contains
        the source code line number of the unit
         */
        Options.v().set_keep_line_number(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_whole_program(true);
        Options.v().set_verbose(true);
        // Jimple body set use the original names
        Options.v().setPhaseOption("jb","use-original-names:true");

        SootClass sootClass = Scene.v().loadClassAndSupport(classname);
        sootClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }
}
