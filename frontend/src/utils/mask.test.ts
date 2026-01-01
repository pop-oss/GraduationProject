/**
 * Property-Based Tests for Data Masking Functions
 * 
 * Feature: frontend-pages, Property 6: 数据脱敏测试
 * Validates: Requirements 3.1, 6.5
 * 
 * Tests that sensitive fields (phone number, ID number, name, email) 
 * are displayed in masked format using consistent masking rules.
 */

import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { maskPhone, maskIdNo, maskName, maskEmail } from './mask';

// Custom arbitrary for generating digit strings of specific length
const digitString = (minLength: number, maxLength: number) =>
  fc.array(fc.integer({ min: 0, max: 9 }), { minLength, maxLength })
    .map(arr => arr.join(''));

// Custom arbitrary for generating ID number strings (digits + X)
const idNoString = (length: number) =>
  fc.array(
    fc.oneof(
      fc.integer({ min: 0, max: 9 }).map(n => n.toString()),
      fc.constant('X')
    ),
    { minLength: length, maxLength: length }
  ).map(arr => arr.join(''));

describe('Property 6: 数据脱敏测试 (Data Masking)', () => {
  /**
   * Property: For any valid phone number (11 digits), the masked result should:
   * 1. Preserve the first 3 digits
   * 2. Preserve the last 4 digits
   * 3. Replace middle digits with exactly 4 asterisks
   * 4. Have total length of 11 characters
   */
  describe('maskPhone - 手机号脱敏', () => {
    it('should mask valid 11-digit phone numbers correctly', () => {
      fc.assert(
        fc.property(
          digitString(11, 11),
          (phone) => {
            const masked = maskPhone(phone);
            // Should preserve first 3 digits
            expect(masked.slice(0, 3)).toBe(phone.slice(0, 3));
            // Should have 4 asterisks in the middle
            expect(masked.slice(3, 7)).toBe('****');
            // Should preserve last 4 digits
            expect(masked.slice(-4)).toBe(phone.slice(-4));
            // Total length should be 11
            expect(masked.length).toBe(11);
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should return empty string for null/undefined/empty input', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(null, undefined, ''),
          (input) => {
            expect(maskPhone(input)).toBe('');
          }
        ),
        { numRuns: 10 }
      );
    });

    it('should return original string for short inputs (< 7 chars)', () => {
      fc.assert(
        fc.property(
          digitString(1, 6),
          (phone) => {
            expect(maskPhone(phone)).toBe(phone);
          }
        ),
        { numRuns: 100 }
      );
    });
  });

  /**
   * Property: For any valid ID number (18 digits), the masked result should:
   * 1. Preserve the first 3 characters
   * 2. Preserve the last 4 characters
   * 3. Replace middle characters with exactly 11 asterisks
   * 4. Have total length of 18 characters
   */
  describe('maskIdNo - 身份证号脱敏', () => {
    it('should mask valid 18-character ID numbers correctly', () => {
      fc.assert(
        fc.property(
          idNoString(18),
          (idNo) => {
            const masked = maskIdNo(idNo);
            // Should preserve first 3 characters
            expect(masked.slice(0, 3)).toBe(idNo.slice(0, 3));
            // Should have 11 asterisks in the middle
            expect(masked.slice(3, 14)).toBe('***********');
            // Should preserve last 4 characters
            expect(masked.slice(-4)).toBe(idNo.slice(-4));
            // Total length should be 18
            expect(masked.length).toBe(18);
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should return empty string for null/undefined/empty input', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(null, undefined, ''),
          (input) => {
            expect(maskIdNo(input)).toBe('');
          }
        ),
        { numRuns: 10 }
      );
    });

    it('should return original string for short inputs (< 8 chars)', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 7 }),
          (idNo) => {
            const trimmed = idNo.trim();
            if (trimmed.length < 8) {
              expect(maskIdNo(idNo)).toBe(trimmed);
            }
          }
        ),
        { numRuns: 100 }
      );
    });
  });

  /**
   * Property: For any name with length > 2, the masked result should:
   * 1. Preserve the first character
   * 2. Preserve the last character
   * 3. Replace middle characters with asterisks
   * 4. Have the same length as the original
   */
  describe('maskName - 姓名脱敏', () => {
    it('should mask names with length > 2 correctly', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
          (name) => {
            const trimmed = name.trim();
            const masked = maskName(name);
            // Should preserve first character
            expect(masked[0]).toBe(trimmed[0]);
            // Should preserve last character
            expect(masked[masked.length - 1]).toBe(trimmed[trimmed.length - 1]);
            // Middle should be all asterisks
            const middle = masked.slice(1, -1);
            expect(middle).toBe('*'.repeat(trimmed.length - 2));
            // Length should be preserved
            expect(masked.length).toBe(trimmed.length);
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should mask 2-character names as first char + asterisk', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 2, maxLength: 2 }).filter(s => s.trim().length === 2),
          (name) => {
            const trimmed = name.trim();
            const masked = maskName(name);
            expect(masked).toBe(trimmed[0] + '*');
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should return original for single character names', () => {
      fc.assert(
        fc.property(
          fc.string({ minLength: 1, maxLength: 1 }),
          (name) => {
            const trimmed = name.trim();
            if (trimmed.length === 1) {
              expect(maskName(name)).toBe(trimmed);
            }
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should return empty string for null/undefined/empty input', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(null, undefined, ''),
          (input) => {
            expect(maskName(input)).toBe('');
          }
        ),
        { numRuns: 10 }
      );
    });
  });

  /**
   * Property: For any valid email, the masked result should:
   * 1. Preserve the first character of username
   * 2. Replace rest of username with ***
   * 3. Preserve the domain part completely
   */
  describe('maskEmail - 邮箱脱敏', () => {
    it('should mask valid emails correctly', () => {
      fc.assert(
        fc.property(
          fc.emailAddress().filter(email => email.indexOf('@') > 1),
          (email) => {
            const masked = maskEmail(email);
            const atIndex = email.indexOf('@');
            const domain = email.slice(atIndex);
            
            // Should preserve first character
            expect(masked[0]).toBe(email[0]);
            // Should have *** after first character
            expect(masked.slice(1, 4)).toBe('***');
            // Should preserve domain
            expect(masked.endsWith(domain)).toBe(true);
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should return empty string for null/undefined/empty input', () => {
      fc.assert(
        fc.property(
          fc.constantFrom(null, undefined, ''),
          (input) => {
            expect(maskEmail(input)).toBe('');
          }
        ),
        { numRuns: 10 }
      );
    });

    it('should return original for emails with very short username', () => {
      fc.assert(
        fc.property(
          fc.constantFrom('a@test.com', '@test.com'),
          (email) => {
            const masked = maskEmail(email);
            const atIndex = email.indexOf('@');
            // For very short usernames, behavior may vary
            if (atIndex <= 1) {
              expect(masked).toBe(email);
            }
          }
        ),
        { numRuns: 10 }
      );
    });
  });

  /**
   * Property: Masking should be idempotent in terms of sensitive data protection
   * Once masked, the original sensitive data should not be recoverable
   */
  describe('Masking Security Properties', () => {
    it('masked phone should not contain original middle digits', () => {
      fc.assert(
        fc.property(
          digitString(11, 11),
          (phone) => {
            const masked = maskPhone(phone);
            const originalMiddle = phone.slice(3, 7);
            // The masked middle should be asterisks, not the original digits
            expect(masked.slice(3, 7)).not.toBe(originalMiddle);
            expect(masked.slice(3, 7)).toBe('****');
          }
        ),
        { numRuns: 100 }
      );
    });

    it('masked ID should not contain original middle characters', () => {
      fc.assert(
        fc.property(
          idNoString(18),
          (idNo) => {
            const masked = maskIdNo(idNo);
            const originalMiddle = idNo.slice(3, 14);
            // The masked middle should be asterisks, not the original characters
            expect(masked.slice(3, 14)).not.toBe(originalMiddle);
            expect(masked.slice(3, 14)).toBe('***********');
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});
