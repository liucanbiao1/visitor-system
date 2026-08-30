import i18n from '../i18n'

// visit_reason is stored in DB as raw English text; map known values to i18n keys
const REASON_KEY_MAP = {
  'Campus Tour': 'reason.campusTour',
  'Academic Exchange': 'reason.academicExchange',
  'Job Interview': 'reason.jobInterview',
  'Lab Open Day': 'reason.labOpenDay',
  'Book Donation': 'reason.bookDonation',
  'Equipment Maintenance': 'reason.equipmentMaintenance',
  'Graduation Ceremony': 'reason.graduationCeremony',
  'Research Collaboration': 'reason.researchCollaboration',
  'Product Sales': 'reason.productSales',
  'Credit Card Promotion': 'reason.creditCardPromotion',
  'Alumni Visit': 'reason.alumniVisit',
  'Second Lab Visit': 'reason.secondLabVisit',
  'Noise Complaint': 'reason.noiseComplaint',
  'Volunteer Activity': 'reason.volunteerActivity',
  'Parent-Teacher Meeting': 'reason.parentTeacherMeeting',
  'Package Pickup': 'reason.packagePickup',
  'Test Cancellation': 'reason.testCancellation',
  'Cancelled Due to Weather': 'reason.cancelledDueToWeather',
  'Network Equipment Check': 'reason.networkEquipmentCheck',
  'University-Enterprise MOU': 'reason.universityEnterpriseMou',
  'Other': 'reason.other',
}

export const REASON_OPTIONS = Object.entries(REASON_KEY_MAP).map(([value, key]) => ({ value, key }))

// AI reject_reason is a fixed enum code from the AI system prompt; map to i18n keys
export const REJECT_KEY_MAP = {
  COMMERCIAL_PROMOTION: 'aiReject.commercialPromotion',
  NON_CAMPUS_PURPOSE: 'aiReject.nonCampusPurpose',
  SUSPICIOUS: 'aiReject.suspicious',
  TIME_CONFLICT: 'aiReject.timeConflict',
  LOW_CONFIDENCE: 'aiReject.lowConfidence',
  OTHER: 'aiReject.other',
}

export function translateReason(name) {
  if (!name) return name
  const key = REASON_KEY_MAP[name]
  return key ? i18n.global.t(key) : name
}

export function translateRejectReason(code) {
  if (!code) return code
  const key = REJECT_KEY_MAP[code]
  return key ? i18n.global.t(key) : code
}
