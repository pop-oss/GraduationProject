import { lazy } from 'react'
import { RouteObject, Navigate } from 'react-router-dom'
import { AuthGuard } from '@/components/AuthGuard'
import { Role } from '@/types'
import AppLayout from '@/layouts/AppLayout'

// 懒加载组件 - 认证页面
const Login = lazy(() => import('@/pages/Login'))
const ForbiddenPage = lazy(() => import('@/pages/ForbiddenPage'))
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'))

// 懒加载组件 - 患者端
const PatientHome = lazy(() => import('@/pages/Patient/Home'))
const PatientAppointment = lazy(() => import('@/pages/Patient/Appointment'))
const PatientConsultation = lazy(() => import('@/pages/Patient/Consultation'))
const PatientConsultationRoom = lazy(() => import('@/pages/Patient/ConsultationRoom'))
const PatientPrescriptions = lazy(() => import('@/pages/Patient/Prescriptions'))
const PatientFollowups = lazy(() => import('@/pages/Patient/Followups'))
const PatientAIChat = lazy(() => import('@/pages/Patient/AIChat'))

// 懒加载组件 - 医生端
const DoctorWorkbench = lazy(() => import('@/pages/Doctor/Workbench'))
const DoctorWaitingList = lazy(() => import('@/pages/Doctor/WaitingList'))
const DoctorConsultation = lazy(() => import('@/pages/Doctor/Consultation'))
const DoctorConsultationRoom = lazy(() => import('@/pages/Doctor/ConsultationRoom'))
const DoctorMedicalRecord = lazy(() => import('@/pages/Doctor/MedicalRecord'))
const DoctorPrescription = lazy(() => import('@/pages/Doctor/Prescription'))
const DoctorReferral = lazy(() => import('@/pages/Doctor/Referral'))
const DoctorMDT = lazy(() => import('@/pages/Doctor/MDT'))

// 懒加载组件 - 药师端
const PharmacistReviewList = lazy(() => import('@/pages/Pharmacist/ReviewList'))
const PharmacistReviewDetail = lazy(() => import('@/pages/Pharmacist/ReviewDetail'))
const PharmacistReviewHistory = lazy(() => import('@/pages/Pharmacist/ReviewHistory'))

// 懒加载组件 - 管理端
const AdminDashboard = lazy(() => import('@/pages/Admin/Dashboard'))
const AdminUserManagement = lazy(() => import('@/pages/Admin/UserManagement'))
const AdminAuditLog = lazy(() => import('@/pages/Admin/AuditLog'))
const AdminRolePermission = lazy(() => import('@/pages/Admin/RolePermission'))

// 懒加载组件 - 统计
const StatsDashboard = lazy(() => import('@/pages/Stats/Dashboard'))

// 懒加载组件 - 通用
const ProfilePage = lazy(() => import('@/pages/Profile'))

/**
 * 带布局的路由包装组件
 */
const LayoutWrapper = () => <AppLayout />

/**
 * 路由配置
 * _Requirements: 1.1, 1.6, 1.7_
 */
const routes: RouteObject[] = [
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '/login', element: <Login /> },
  
  // 患者端路由
  {
    path: '/patient',
    element: (
      <AuthGuard allowedRoles={[Role.PATIENT]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <PatientHome /> },
      { path: 'appointment', element: <PatientAppointment /> },
      { path: 'consultations', element: <PatientConsultation /> },
      { path: 'consultation/:id', element: <PatientConsultation /> },
      { path: 'consultation/:id/room', element: <PatientConsultationRoom /> },
      { path: 'prescriptions', element: <PatientPrescriptions /> },
      { path: 'followups', element: <PatientFollowups /> },
      { path: 'ai-chat', element: <PatientAIChat /> },
    ],
  },

  // 医生端路由
  {
    path: '/doctor',
    element: (
      <AuthGuard allowedRoles={[Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <DoctorWorkbench /> },
      { path: 'waiting', element: <DoctorWaitingList /> },
      { path: 'consultation/:id', element: <DoctorConsultation /> },
      { path: 'consultation/:id/room', element: <DoctorConsultationRoom /> },
      { path: 'consultation/:id/record', element: <DoctorMedicalRecord /> },
      { path: 'prescription/:consultationId', element: <DoctorPrescription /> },
      { path: 'referral', element: <DoctorReferral /> },
      { path: 'mdt', element: <DoctorMDT /> },
    ],
  },

  // 药师端路由
  {
    path: '/pharmacist',
    element: (
      <AuthGuard allowedRoles={[Role.PHARMACIST]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <PharmacistReviewList /> },
      { path: 'review/:id', element: <PharmacistReviewDetail /> },
      { path: 'history', element: <PharmacistReviewHistory /> },
    ],
  },

  // 管理端路由
  {
    path: '/admin',
    element: (
      <AuthGuard allowedRoles={[Role.ADMIN]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <AdminDashboard /> },
      { path: 'users', element: <AdminUserManagement /> },
      { path: 'roles', element: <AdminRolePermission /> },
      { path: 'audit-log', element: <AdminAuditLog /> },
    ],
  },

  // 统计分析路由（管理员可访问）
  {
    path: '/stats',
    element: (
      <AuthGuard allowedRoles={[Role.ADMIN]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <StatsDashboard /> },
    ],
  },

  // 个人中心路由（所有登录用户可访问）
  {
    path: '/profile',
    element: (
      <AuthGuard allowedRoles={[Role.ADMIN, Role.PATIENT, Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT, Role.PHARMACIST]}>
        <LayoutWrapper />
      </AuthGuard>
    ),
    children: [
      { index: true, element: <ProfilePage /> },
    ],
  },

  // 错误页面
  { path: '/403', element: <ForbiddenPage /> },
  { path: '*', element: <NotFoundPage /> },
]

export default routes
