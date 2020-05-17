package io.fabric8.demo;

import io.fabric8.demo.controller.PodCloneController;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PodCloneMain {
    public static Logger logger = Logger.getLogger(PodCloneMain.class.getName());

    public static void main(String[] args) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = client.getNamespace();
            if (namespace == null) {
                logger.log(Level.INFO, "No namespace found via config, assuming default.");
                namespace = "default";
            }

            logger.log(Level.INFO, "Using namespace : " + namespace);

            SharedInformerFactory informerFactory = client.informers();

            SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class, PodList.class, 10 * 60 * 1000);
            PodCloneController podSetController = new PodCloneController(client, podSharedIndexInformer, namespace);

            podSetController.create();
            informerFactory.startAllRegisteredInformers();

            podSetController.run();
        }
    }

}
