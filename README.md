# api.yatopia.net
RESTful api

# Routes
V1 ROUTES:
<br>
`GET /latestCommit?branch=:branch` - gives latest commit's sha for the specified branch
<br>
`GET /latestBuild?branch=:branch` - gives information about the latest build for the specified branch

V2 ROUTES:
<br>
V2 only focuses on builds and build downloads, if you want to get only commits you can use V1. We're
not going to drop it.
<br>
`GET /v2/latestBuild?branch=:branch` - gives information about the latest build for the specified branch
<br>
`GET /v2/latestBuild/download?branch=:branch` - downloads the latest build for the specified branch