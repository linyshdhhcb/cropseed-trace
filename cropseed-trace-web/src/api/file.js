import request from "./request";

// 上传文件
export function uploadFile(file, folder = "") {
  const formData = new FormData();
  formData.append("file", file);
  if (folder) {
    formData.append("folder", folder);
  }

  return request({
    url: "/file/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// 批量上传文件
export function uploadBatch(files, folder = "") {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append("files", file);
  });
  if (folder) {
    formData.append("folder", folder);
  }

  return request({
    url: "/file/upload/batch",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// 下载文件
export function downloadFile(objectName) {
  return request({
    url: "/file/download",
    method: "get",
    params: { objectName },
    responseType: "blob",
  });
}

// 删除文件
export function deleteFile(objectName) {
  return request({
    url: "/file/delete",
    method: "delete",
    params: { objectName },
  });
}

// 获取文件预签名URL
export function getPresignedUrl(objectName, expiry = 60) {
  return request({
    url: "/file/presigned-url",
    method: "get",
    params: { objectName, expiry },
  });
}

// 获取文件列表
export function getFileList(prefix = "") {
  return request({
    url: "/file/list",
    method: "get",
    params: { prefix },
  });
}
