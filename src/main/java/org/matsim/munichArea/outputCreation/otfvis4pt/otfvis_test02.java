package org.matsim.munichArea.outputCreation.otfvis4pt;
/**
 * Created by key on 2017/07/03.
 */



import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigReader;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;


public class otfvis_test02 {
    public static void main(String[] args) {
//       OTFVisTest01();
//        OTFVisTest02();
        OTFVisTest03();

//        visExample(args[0]);
    }



    public static void OTFVisTest01() {
        //comment on classpath
        // http://www.k-cube.co.jp/wakaba/server/class_path.html

        //		org.matsim.run.OTFVis.main(new String[] {"-convert", "/output/ITERS/it.10/10.events.xml.gz", "/output/output_network.xml.gz", "/output/MVI/10.visualization.mvi", "600"});
//        org.matsim.contrib.otfvis.OTFVis.main(new String[] {"-convert", "./output/ITERS/it.10/10.events.xml.gz", "./output/output_network.xml.gz", "./output/MVI/10.visualization.mvi", "600"});
//        org.matsim.contrib.otfvis.OTFVis.main(new String[] {"-convert", "./output/ITERS/it.10/10.events.xml.gz", "./output/output_network.xml.gz", "./output/MVI/10.visualization.mvi", "10"});
//        OTFVis.main(new String[] {"-convert", "./output/output_events.xml.gz", "./output/output_network.xml.gz", "./output/MVI/event.visualization.mvi", "10"});
        OTFVis.main(new String[] {"-convert", "./output/TF0.90CF0.90SF0.90IT50test1/test1_2010.output_events.xml.gz", "./output/TF0.90CF0.90SF0.90IT50test1/test1_2010.output_network.xml.gz", "./output/TF0.90CF0.90SF0.90IT50test1/MVI/event.visualization.mvi", "10"});
        //takes about 20 minutes for kagawa case

//        OTFVis.main(new String[] {"./input/studyNetworkLight.xml"});
//        OTFVis.main(new String[] {"./input/KagawaNetworkDense.xml"});


        //        OTFVis("-convert", "./output/ITERS/it.10/10.events.xml.gz", "./output/output_network.xml.gz", "./output/MVI/10.visualization.mvi", "600");
        //          does not work: probably the location of OTFVis is different so failing to import the method.

    }

    public static void OTFVisTest02() {
        org.matsim.contrib.otfvis.OTFVisGUI.runDialog();
    }


    public static void OTFVisTest03() {
//        org.matsim.contrib.otfvis.OTFVis.main(new String[] {"./output/MVI/10.visualization.mvi"});
        org.matsim.contrib.otfvis.OTFVis.main(new String[] {"./output/TF0.90CF0.90SF0.90IT50test1/MVI/event.visualization.mvi"});
    }


    //https://github.com/matsim-org/playgrounds/blob/master/tthunig/src/main/java/signals/laemmer/run/LaemmerBasicExample.java

    public static void visExample(String configPath) {
        Config config = ConfigUtils.createConfig();
        ConfigUtils.loadConfig(config, configPath);

        config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.withHoles);
        config.qsim().setNodeOffset(5.);
        OTFVisConfigGroup otfvisConfig =
                ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.GROUP_NAME, OTFVisConfigGroup.class);
        otfvisConfig.setScaleQuadTreeRect(true);
//            otfvisConfig.setColoringScheme(OTFVisConfigGroup.ColoringScheme.byId);
//            otfvisConfig.setAgentSize(240);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        OTFVis.playScenario(scenario);
    }



}
