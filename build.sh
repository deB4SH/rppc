#! /bin/bash
# defaults
REGISTRY="ghcr.io/deb4sh"
# parse options
while getopts :r:p: flag
do
    # shellcheck disable=SC2220
    case "${flag}" in
        r) REGISTRY=${OPTARG};;
        p) PUSH=${OPTARG};;
    esac
done
# get current tag information
IS_DEV_BUILD=$(git tag -l --contains HEAD)
GIT_TAG=$(git describe --abbrev=0 --tags HEAD)

if [ -z "$IS_DEV_BUILD" ]
then
    TIMESTAMP=$(date +%s)
    TAG=$(echo "$GIT_TAG"-"$TIMESTAMP")
else
    TAG=$GIT_TAG
fi

echo "Building image with tag $TAG"

# shellcheck disable=SC2046
docker \
    build . \
    -f ./Dockerfile \
    -t $(echo "$REGISTRY/rppc:$TAG")

if [ -n "$PUSH" ]; then
  # shellcheck disable=SC2046
  docker push $(echo "$REGISTRY/rppc:$TAG")
fi