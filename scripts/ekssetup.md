```
    eksctl create cluster --name=my-eks22 \
                      --region=ap-southeast-1 \
                      --zones=ap-southeast-1a,ap-southeast-1b \
                      --version=1.30 \
                      --without-nodegroup
```

```
eksctl utils associate-iam-oidc-provider \
    --region ap-southeast-1 \
    --cluster my-eks22 \
    --approve
```

```
eksctl create nodegroup --cluster=my-eks22 \
                       --region=ap-southeast-1 \
                       --name=node2 \
                       --node-type=t3.medium \
                       --nodes=3 \
                       --nodes-min=2 \
                       --nodes-max=4 \
                       --node-volume-size=20 \
                       --ssh-access \
                       --ssh-public-key=login-ap-southeast \
                       --managed \
                       --asg-access \
                       --external-dns-access \
                       --full-ecr-access \
                       --appmesh-access \
                       --alb-ingress-access
```