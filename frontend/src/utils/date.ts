/**
 * 日期处理工具函数
 */

import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

// 配置 dayjs
dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

/**
 * 格式化日期时间
 * @param date 日期字符串或 Date 对象
 * @param format 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDateTime(
  date: string | Date | null | undefined,
  format = 'YYYY-MM-DD HH:mm:ss'
): string {
  if (!date) return '';
  return dayjs(date).format(format);
}

/**
 * 格式化日期（不含时间）
 * @param date 日期字符串或 Date 对象
 * @param format 格式化模板，默认 'YYYY-MM-DD'
 * @returns 格式化后的日期字符串
 */
export function formatDate(
  date: string | Date | null | undefined,
  format = 'YYYY-MM-DD'
): string {
  if (!date) return '';
  return dayjs(date).format(format);
}

/**
 * 格式化时间（不含日期）
 * @param date 日期字符串或 Date 对象
 * @param format 格式化模板，默认 'HH:mm:ss'
 * @returns 格式化后的时间字符串
 */
export function formatTime(
  date: string | Date | null | undefined,
  format = 'HH:mm:ss'
): string {
  if (!date) return '';
  return dayjs(date).format(format);
}

/**
 * 获取相对时间描述
 * @param date 日期字符串或 Date 对象
 * @returns 相对时间描述，如 "3分钟前"、"2小时前"
 */
export function fromNow(date: string | Date | null | undefined): string {
  if (!date) return '';
  return dayjs(date).fromNow();
}

/**
 * 判断日期是否为今天
 * @param date 日期字符串或 Date 对象
 * @returns 是否为今天
 */
export function isToday(date: string | Date | null | undefined): boolean {
  if (!date) return false;
  return dayjs(date).isSame(dayjs(), 'day');
}

/**
 * 判断日期是否已过期
 * @param date 日期字符串或 Date 对象
 * @returns 是否已过期
 */
export function isExpired(date: string | Date | null | undefined): boolean {
  if (!date) return false;
  return dayjs(date).isBefore(dayjs());
}

/**
 * 获取两个日期之间的天数差
 * @param start 开始日期
 * @param end 结束日期
 * @returns 天数差
 */
export function daysBetween(
  start: string | Date | null | undefined,
  end: string | Date | null | undefined
): number {
  if (!start || !end) return 0;
  return dayjs(end).diff(dayjs(start), 'day');
}
