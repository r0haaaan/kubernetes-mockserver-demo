package io.fabric8.demo;

import io.fabric8.demo.controller.PodCloneController;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PodCloneMain {
    public static final Logger logger = Logger.getLogger(PodCloneMain.class.getName());

    public static void main(String[] args) {
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            String namespace = client.getNamespace();
            if (namespace == null) {
                logger.log(Level.INFO, "No namespace found via config, assuming default.");
                namespace = "default";
            }

            logger.log(Level.INFO, "Using namespace : " + namespace);

            SharedInformerFactory informerFactory = client.informers();

            SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class,10 * 60 * 1000);
            PodCloneController podSetController = new PodCloneController(client, podSharedIndexInformer, namespace);

            podSetController.create();
            informerFactory.startAllRegisteredInformers();

            podSetController.run();
        }
    }

}
