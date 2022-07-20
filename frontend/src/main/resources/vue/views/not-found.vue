<template id="not-found">
  <div class="ui container">
    <app-header></app-header>
    <h1>Page not found (error 404)</h1>
  </div>
</template>
<head>
  <meta name="color-scheme" content="light dark">
  <title>404 Not Found</title>
</head>
<script>
Vue.component("not-found", {template: "#not-found"});
</script>