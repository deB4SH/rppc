Kubernetes Deployment
===

This deployment heavyly resides on kustomize components. If you are not familiar with this setup please take a quick look at the official documentation: https://github.com/kubernetes-sigs/kustomize/blob/master/examples/components.md#components-example

The deployment itself provides two basic examples.
* example_enterprise
    * provides an additional servicemonitor for prometheus
* example_without_metrics
    * does not run the metrics container
    * does only ping-pong messages

To build one or the other call one of the following commands:
* `kustomize build overlay/example_enterprise` 
* `kustomize build overlay/example_without_metrics` 

You can also view the results of both overlays within the **.output** folder