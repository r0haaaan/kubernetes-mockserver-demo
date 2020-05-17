package io.fabric8.demo.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PodCloneController {
    private BlockingQueue<String> workqueue;
    private SharedIndexInformer<Pod> podInformer;
    private Lister<Pod> podLister;
    private KubernetesClient kubernetesClient;
    public static Logger logger = Logger.getLogger(PodCloneController.class.getName());

    public PodCloneController(KubernetesClient kubernetesClient, SharedIndexInformer<Pod> podInformer, String namespace) {
        this.kubernetesClient = kubernetesClient;
        this.podLister = new Lister<>(podInformer.getIndexer(), namespace);
        this.podInformer = podInformer;
        this.workqueue = new ArrayBlockingQueue<>(1024);
    }

    public void create() {
        podInformer.addEventHandler(new ResourceEventHandler<Pod>() {
            @Override
            public void onAdd(Pod pod) {
                logger.log(Level.INFO, "onAdd");
                enqueuePodObject(pod);
            }

            @Override
            public void onUpdate(Pod oldPod, Pod newPod) {
                logger.log(Level.INFO, "onUpdate");
            }

            @Override
            public void onDelete(Pod pod, boolean b) { }
        });
    }

    public void run() {
        logger.log(Level.INFO, "Starting PodClone controller");
        while (!podInformer.hasSynced()) logger.log(Level.INFO, "not synced");

        while (true) {
            try {
                logger.log(Level.INFO, "trying to fetch item from workqueue...");
                if (workqueue.isEmpty()) {
                    logger.log(Level.INFO, "Work Queue is empty");
                }
                String key = workqueue.take();
                logger.log(Level.INFO, "Got " + key);
                if (key == null || key.isEmpty() || (!key.contains("/"))) {
                    logger.log(Level.WARNING, "invalid resource key: " + key);
                }

                // Get the PodSet resource's name from key which is in format namespace/name
                String name = key.split("/")[1];
                Pod pod = podLister.get(key.split("/")[1]);
                if (pod == null) {
                    logger.log(Level.SEVERE, "Pod " + name + " in workqueue no longer exists");
                    return;
                }
                reconcile(pod);

            } catch (InterruptedException interruptedException) {
                logger.log(Level.SEVERE, "controller interrupted..");
            }
        }
    }

    /**
     * Tries to achieve clone pod provided.
     *
     * @param pod provided Pod
     */
    protected void reconcile(Pod pod) {
        if (pod.getMetadata().getLabels() != null && pod.getMetadata().getLabels().containsKey("clone")) {
            return;
        }
        Pod clonePod = new PodBuilder()
                .withNewMetadata()
                .withGenerateName(pod.getMetadata().getName() + "-clone")
                .addToLabels("clone", "true")
                .endMetadata()
                .withSpec(pod.getSpec())
                .build();
        logger.info("Creating clone for " + pod.getMetadata().getName());
        kubernetesClient.pods().inNamespace(pod.getMetadata().getNamespace()).create(clonePod);
    }

    private void enqueuePodObject(Pod pod) {
        logger.log(Level.INFO, "enqueuePod(" + pod.getMetadata().getName() + ")");
        String key = Cache.metaNamespaceKeyFunc(pod);
        logger.log(Level.INFO, "Going to enqueue key " + key);
        if (key != null || !key.isEmpty()) {
            logger.log(Level.INFO, "Adding item to workqueue");
            workqueue.add(key);
        }
    }
}
