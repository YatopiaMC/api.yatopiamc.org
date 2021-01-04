# api.yatopiamc.org
RESTful api

# Routes
V1 ROUTES:
<br>
`GET /latestCommit?branch=:branch` - gives latest commit's sha of the specified branch
<br>
`GET /latestBuild?branch=:branch` - gives information about the latest build of the specified branch

V2 ROUTES:
<br>
V2 only focuses on builds and build downloads, if you want to get only commits you can use V1. We're
not going to drop it.
<br>
`GET /v2/latestBuild?branch=:branch` - gives information about the latest build of the specified branch
<br>
`GET /v2/latestBuild/download?branch=:branch` - downloads the latest build of the specified branch
<br>
`GET /v2/builds?branch=:branch&onlySuccessful=<true/false>` - gives information about the last 10 builds of the specified branch
<br>
`GET /v2/build/:build?branch=:branch` - gives information about the specified build of the specified branch.
<br>
`GET /v2/build/:build/download?branch=:branch` - downloads the specified build of the specified branch
<br>
`GET /v2/stableBuild?branch=:branch` - gives information about the latest stable build of the specified branch
<br>
`GET /v2/stableBuild/download?branch=:branch` - downloads the latest stable build of the specified branch