package io.fabric8.demo.kubernetes.customresource;

public class CronTabStatus {
    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    private int replicas;

    public String getLabelSelector() {
        return labelSelector;
    }

    public void setLabelSelector(String labelSelector) {
        this.labelSelector = labelSelector;
    }

    private String labelSelector;

    @Override
    public String toString() {
        return "CronTabStatus{" +
          " replicas=" + replicas +
          " , labelSelector='" + labelSelector + "'" +
          "}";
    }
}
