/**
 * 数据脱敏工具函数
 */

/**
 * 手机号脱敏
 * 将手机号中间4位替换为****
 * @param phone 手机号
 * @returns 脱敏后的手机号，如 138****1234
 */
export function maskPhone(phone: string | null | undefined): string {
  if (!phone) return '';
  const str = phone.trim();
  if (str.length < 7) return str;
  // 保留前3位和后4位
  return str.slice(0, 3) + '****' + str.slice(-4);
}

/**
 * 身份证号脱敏
 * 将身份证号中间部分替换为****
 * @param idNo 身份证号
 * @returns 脱敏后的身份证号，如 110***********1234
 */
export function maskIdNo(idNo: string | null | undefined): string {
  if (!idNo) return '';
  const str = idNo.trim();
  if (str.length < 8) return str;
  // 保留前3位和后4位
  return str.slice(0, 3) + '***********' + str.slice(-4);
}

/**
 * 姓名脱敏
 * 将姓名中间部分替换为*
 * @param name 姓名
 * @returns 脱敏后的姓名，如 张*三
 */
export function maskName(name: string | null | undefined): string {
  if (!name) return '';
  const str = name.trim();
  if (str.length <= 1) return str;
  if (str.length === 2) return str[0] + '*';
  // 保留首尾字符
  return str[0] + '*'.repeat(str.length - 2) + str[str.length - 1];
}

/**
 * 邮箱脱敏
 * 将邮箱用户名部分脱敏
 * @param email 邮箱
 * @returns 脱敏后的邮箱，如 t***@example.com
 */
export function maskEmail(email: string | null | undefined): string {
  if (!email) return '';
  const str = email.trim();
  const atIndex = str.indexOf('@');
  if (atIndex <= 1) return str;
  const username = str.slice(0, atIndex);
  const domain = str.slice(atIndex);
  if (username.length <= 2) return username[0] + '***' + domain;
  return username[0] + '***' + domain;
}
