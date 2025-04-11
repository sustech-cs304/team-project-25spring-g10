<template>
  <div 
    class="toggle-switch" 
    :class="{ 'active': modelValue, 'disabled': disabled }"
    @click="toggleValue"
  >
    <div class="toggle-track">
      <div class="toggle-indicator"></div>
    </div>
    <div class="toggle-label" v-if="label">{{ label }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  color: {
    type: String,
    default: 'primary' // primary, success, warning, error
  }
});

const emit = defineEmits(['update:modelValue']);

const toggleValue = () => {
  if (!props.disabled) {
    emit('update:modelValue', !props.modelValue);
  }
};
</script>

<style scoped>
.toggle-switch {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  gap: var(--space-sm);
  user-select: none;
}

.toggle-switch.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-track {
  position: relative;
  width: 38px;
  height: 20px;
  border-radius: 20px;
  background-color: var(--neutral-300);
  transition: all 0.3s ease;
}

.toggle-indicator {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: white;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.toggle-switch.active .toggle-track {
  background-color: var(--primary-color);
}

.toggle-switch.active.color-success .toggle-track {
  background-color: var(--success);
}

.toggle-switch.active.color-warning .toggle-track {
  background-color: var(--warning);
}

.toggle-switch.active.color-error .toggle-track {
  background-color: var(--error);
}

.toggle-switch.active .toggle-indicator {
  transform: translateX(18px);
}

.toggle-switch:hover .toggle-track {
  background-color: var(--neutral-400);
}

.toggle-switch.active:hover .toggle-track {
  background-color: var(--primary-dark);
}

.toggle-switch.active.color-success:hover .toggle-track {
  background-color: var(--success-dark);
}

.toggle-switch.active.color-warning:hover .toggle-track {
  background-color: var(--warning-dark);
}

.toggle-switch.active.color-error:hover .toggle-track {
  background-color: var(--error-dark);
}

.toggle-label {
  font-size: 0.9rem;
  color: var(--neutral-700);
}

.toggle-switch.active .toggle-label {
  color: var(--neutral-900);
}
</style> 