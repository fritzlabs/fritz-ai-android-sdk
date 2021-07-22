#!/bin/bash
set -e

s3destinationpath=s3://fritz-docs/android/$1/
builddokkapath=./build/dokka/-fritz-s-d-k/

aws s3 cp ./build/dokka/-fritz-s-d-k/ $s3destinationpath --recursive --only-show-errors

# write the redirects to this version
echo Pushing release version $1
sed -e "s/RELEASE_VERSION/$1/g" ./deploy/redirect.html > ./index.html
aws s3 cp ./index.html s3://fritz-docs/android/index.html --only-show-errors
aws s3 cp ./index.html s3://fritz-docs/android/latest/index.html --only-show-errors

echo Success
