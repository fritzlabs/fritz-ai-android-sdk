#!/bin/bash
set -e

# echo Generating docs for $CI_BRANCH
./gradlew dokka

style=`cat ./deploy/docs/style.html`
head=`cat ./deploy/docs/head.html`
header=`cat ./deploy/docs/header.html`
footer=`cat ./deploy/docs/footer.html`

mv ./build/dokka/style.css ./build/dokka/-fritz-s-d-k/style.css

# Manually edit the docs
find ./build/dokka -name "*.html" -type f -exec sed -i "/style.css/s/.*/STYLE_CSS_TO_REPLACE/"  {} \;
find ./build/dokka -name "*.html" -type f -exec sed -i "s|STYLE_CSS_TO_REPLACE|$style|g"  {} \;
find ./build/dokka -name "*.html" -type f -exec sed -i "s|</HEAD>|$head</HEAD>|g" {} \;
find ./build/dokka -name "*.html" -type f -exec sed -i "s|<BODY>|<BODY>$header|g" {} \;
find ./build/dokka -name "*.html" -type f -exec sed -i "s|</BODY>|$footer</BODY>|g" {} \;
