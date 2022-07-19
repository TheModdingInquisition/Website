<template id="members">
  <div class="members">
    <h1>The inquisition's members: {{mems}}</h1>
  </div>
</template>
<head>
  <title>Inquisition's members</title>
  <meta name="color-scheme" content="light dark">
  <link rel="icon" href="/favicon.png" type="image/x-icon">
  <meta name="description" content="The great people behind the inquisition!">
</head>
<script>
const path = "/client_api/get_org_members"
new LoadableData(path).refresh(true, e => {})
const members = JSON.parse(localStorage.getItem("LoadableData:" + path))
    .map(e => e.login)
const asString = members.join(", ")
Vue.component("members", {
  template: "#members",
  data: () => ({
    mems: asString
  })
});
</script>
<style>
</style>