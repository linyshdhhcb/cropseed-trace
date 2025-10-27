// 验证是否为外部链接
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path);
}

// 验证用户名
export function validUsername(str) {
  const valid_map = ["admin", "editor"];
  return valid_map.indexOf(str.trim()) >= 0;
}

// 验证邮箱
export function validEmail(email) {
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return reg.test(email);
}

// 验证手机号
export function validMobile(mobile) {
  const reg = /^1[3-9]\d{9}$/;
  return reg.test(mobile);
}

// 验证身份证号
export function validIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  return reg.test(idCard);
}

// 验证URL
export function validURL(url) {
  const reg =
    /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/;
  return reg.test(url);
}

// 验证小写字母
export function validLowerCase(str) {
  const reg = /^[a-z]+$/;
  return reg.test(str);
}

// 验证大写字母
export function validUpperCase(str) {
  const reg = /^[A-Z]+$/;
  return reg.test(str);
}

// 验证字母
export function validAlphabets(str) {
  const reg = /^[A-Za-z]+$/;
  return reg.test(str);
}

// 验证数字
export function validNumber(str) {
  const reg = /^\d+$/;
  return reg.test(str);
}

// 验证密码强度
export function validPassword(password) {
  // 至少8位，包含大小写字母、数字和特殊字符
  const reg =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  return reg.test(password);
}
