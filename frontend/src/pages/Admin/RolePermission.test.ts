/**
 * RolePermission 组件单元测试
 * 
 * Feature: admin-role-permission
 * Validates: Requirements 2.2, 2.3
 */

import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import { convertToTreeData, getAllPermissionIds } from './rolePermission.utils'
import type { Permission } from '@/services/admin'

// 生成随机权限数据的 arbitrary
const permissionArb = (depth: number = 0): fc.Arbitrary<Permission> =>
  fc.record({
    id: fc.integer({ min: 1, max: 1000 }),
    permCode: fc.string({ minLength: 3, maxLength: 20 }).map(s => s.replace(/\s/g, '_')),
    permName: fc.string({ minLength: 2, maxLength: 30 }).map(s => s.trim() || '权限'),
    permType: fc.constantFrom('menu', 'button') as fc.Arbitrary<'menu' | 'button'>,
    parentId: fc.integer({ min: 0, max: 100 }),
    sortOrder: fc.integer({ min: 0, max: 100 }),
    children: depth < 2 
      ? fc.option(fc.array(permissionArb(depth + 1), { minLength: 0, maxLength: 3 }), { nil: undefined })
      : fc.constant(undefined),
  })

describe('convertToTreeData - 权限树转换', () => {
  /**
   * Property: 转换后的树节点数量应与原始权限数量一致
   * **Validates: Requirements 2.2**
   */
  it('should preserve the number of nodes after conversion', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 0, maxLength: 10 }),
        (permissions) => {
          const treeData = convertToTreeData(permissions)
          expect(treeData.length).toBe(permissions.length)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 每个转换后的节点应包含 key 和 title
   * **Validates: Requirements 2.3**
   */
  it('should include key and title for each node', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 1, maxLength: 10 }),
        (permissions) => {
          const treeData = convertToTreeData(permissions)
          
          const checkNode = (nodes: ReturnType<typeof convertToTreeData>) => {
            nodes.forEach(node => {
              expect(node.key).toBeDefined()
              expect(node.title).toBeDefined()
              expect(typeof node.title).toBe('string')
              if (node.children) {
                checkNode(node.children)
              }
            })
          }
          
          checkNode(treeData)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 节点的 title 应包含权限名称和权限编码
   * **Validates: Requirements 2.3**
   */
  it('should include permName and permCode in title', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 1, maxLength: 10 }),
        (permissions) => {
          const treeData = convertToTreeData(permissions)
          
          permissions.forEach((perm, index) => {
            const title = treeData[index].title as string
            expect(title).toContain(perm.permName)
            expect(title).toContain(perm.permCode)
          })
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 节点的 key 应等于权限的 id
   * **Validates: Requirements 2.2**
   */
  it('should use permission id as node key', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 1, maxLength: 10 }),
        (permissions) => {
          const treeData = convertToTreeData(permissions)
          
          permissions.forEach((perm, index) => {
            expect(treeData[index].key).toBe(perm.id)
          })
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 空数组应返回空数组
   */
  it('should return empty array for empty input', () => {
    const result = convertToTreeData([])
    expect(result).toEqual([])
  })
})

describe('getAllPermissionIds - 获取所有权限ID', () => {
  /**
   * Property: 返回的ID数量应等于所有权限节点的总数（包括子节点）
   * **Validates: Requirements 2.2**
   */
  it('should return all permission IDs including children', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 0, maxLength: 10 }),
        (permissions) => {
          const ids = getAllPermissionIds(permissions)
          
          // 计算总节点数
          const countNodes = (perms: Permission[]): number => {
            return perms.reduce((sum, p) => {
              return sum + 1 + (p.children ? countNodes(p.children) : 0)
            }, 0)
          }
          
          expect(ids.length).toBe(countNodes(permissions))
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 返回的ID应该都是数字类型
   */
  it('should return only number type IDs', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 1, maxLength: 10 }),
        (permissions) => {
          const ids = getAllPermissionIds(permissions)
          ids.forEach(id => {
            expect(typeof id).toBe('number')
          })
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 每个权限的ID都应该在返回结果中
   */
  it('should include every permission ID in result', () => {
    fc.assert(
      fc.property(
        fc.array(permissionArb(), { minLength: 1, maxLength: 10 }),
        (permissions) => {
          const ids = getAllPermissionIds(permissions)
          
          const checkIncluded = (perms: Permission[]) => {
            perms.forEach(p => {
              expect(ids).toContain(p.id)
              if (p.children) {
                checkIncluded(p.children)
              }
            })
          }
          
          checkIncluded(permissions)
        }
      ),
      { numRuns: 100 }
    )
  })

  /**
   * Property: 空数组应返回空数组
   */
  it('should return empty array for empty input', () => {
    const result = getAllPermissionIds([])
    expect(result).toEqual([])
  })
})
