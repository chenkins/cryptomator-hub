<template>
  <p>Hello!</p>
</template>

<script setup lang="ts">
import backend from '../common/backend';

async function foobar() {
  await backend.users.listAll();
}


</script>