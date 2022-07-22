<template id="index">
  <div class="ui container">
    <app-header></app-header>
    <h1>A team of modders trying their best at helping others improve their mods.</h1>
    <app-footer />
  </div>
</template>
<head>
  <title>The Modding Inquisition</title>
  <meta name="color-scheme" content="light dark">
  <link rel="icon" href="/favicon.png" type="image/x-icon">
  <meta name="description" content="Nobody expects the Modding Inquisition!">
</head>
<script>
Vue.component("index", {
  template: "#index"
});
window.onload = () => updateHeader('home')
</script>
<style>

</style>