<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fabric } from 'fabric';

const originalFilename = this.imageUrl.split('/').pop().split('?')[0];
let newFilename = originalFilename;
if (!/\.\w+$/.test(originalFilename)) {
  newFilename += '.jpg';
}
newFilename = newFilename.replace(/\.\w+$/, `_${Date.now()}.jpg`);
const file = new File([imageBlob], newFilename, { type: 'image/jpeg' });
console.log('[DEBUG] originalFilename:', originalFilename);
console.log('[DEBUG] newFilename:', newFilename);
return { file, filename: newFilename };

const addSticker = async (stickerUrl: string) => {
  if (!canvas.value) return;
  
  try {
    const response = await fetch(stickerUrl);
    const blob = await response.blob();
    const imgUrl = URL.createObjectURL(blob);
    
    fabric.Image.fromURL(imgUrl, (img) => {
      // 创建删除按钮
      const deleteBtn = new fabric.Circle({
        radius: 12,
        fill: '#e74c3c',
        left: img.width! - 12,
        top: -12,
        selectable: false,
        evented: true,
        hoverCursor: 'pointer'
      });

      // 创建 X 文本
      const deleteText = new fabric.Text('×', {
        fontSize: 16,
        fill: 'white',
        left: img.width! - 12,
        top: -12,
        selectable: false,
        evented: true,
        hoverCursor: 'pointer',
        originX: 'center',
        originY: 'center'
      });

      // 创建组
      const group = new fabric.Group([img, deleteBtn, deleteText], {
        left: 100,
        top: 100,
        selectable: true,
        hasControls: true,
        hasBorders: true
      });

      // 添加点击事件处理
      group.on('mousedown', function(e) {
        if (e.target === deleteBtn || e.target === deleteText) {
          canvas.value?.remove(group);
          canvas.value?.renderAll();
        }
      });

      canvas.value.add(group);
      canvas.value.setActiveObject(group);
      canvas.value.renderAll();
    });
  } catch (error) {
    console.error('Error loading sticker:', error);
  }
};
</script>

<template>
  <div class="image-editor">
    <!-- ... existing template code ... -->
    <div class="toolbar">
      <!-- ... existing toolbar buttons ... -->
      <button 
        class="toolbar-btn"
        :class="{ active: removeStickerMode }"
        @click="removeStickerMode ? cancelRemoveSticker() : handleRemoveSticker()"
      >
        <span class="material-icons">{{ removeStickerMode ? 'cancel' : 'remove_circle' }}</span>
        {{ removeStickerMode ? '退出删除' : '删除贴纸' }}
      </button>
    </div>
    <!-- ... rest of the template ... -->
  </div>
</template>

<style scoped>
/* ... existing styles ... */
.toolbar-btn.active {
  background-color: #e74c3c;
  color: white;
}
</style> 