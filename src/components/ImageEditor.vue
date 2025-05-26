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