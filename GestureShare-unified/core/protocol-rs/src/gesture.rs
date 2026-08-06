use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq, Hash)]
pub enum GestureType {
    Palm,
    OpenHand,
    ClosedHand,
    Point,
    SwipeLeft,
    SwipeRight,
    Push,
    Pull,
    Grab,
    Throw,
    Pinch,
    PeaceSign,
    ThumbUp,
    ThumbDown,
    Wave,
    Circle,
    Fist,
    TwoFingers,
    OpenPalm,
    Custom,
}

impl GestureType {
    pub fn as_str(&self) -> &'static str {
        match self {
            GestureType::Palm => "palm",
            GestureType::OpenHand => "open_hand",
            GestureType::ClosedHand => "closed_hand",
            GestureType::Point => "point",
            GestureType::SwipeLeft => "swipe_left",
            GestureType::SwipeRight => "swipe_right",
            GestureType::Push => "push",
            GestureType::Pull => "pull",
            GestureType::Grab => "grab",
            GestureType::Throw => "throw",
            GestureType::Pinch => "pinch",
            GestureType::PeaceSign => "peace_sign",
            GestureType::ThumbUp => "thumb_up",
            GestureType::ThumbDown => "thumb_down",
            GestureType::Wave => "wave",
            GestureType::Circle => "circle",
            GestureType::Fist => "fist",
            GestureType::TwoFingers => "two_fingers",
            GestureType::OpenPalm => "open_palm",
            GestureType::Custom => "custom",
        }
    }

    pub fn from_str(s: &str) -> Option<Self> {
        match s {
            "palm" => Some(GestureType::Palm),
            "open_hand" => Some(GestureType::OpenHand),
            "closed_hand" => Some(GestureType::ClosedHand),
            "point" => Some(GestureType::Point),
            "swipe_left" => Some(GestureType::SwipeLeft),
            "swipe_right" => Some(GestureType::SwipeRight),
            "push" => Some(GestureType::Push),
            "pull" => Some(GestureType::Pull),
            "grab" => Some(GestureType::Grab),
            "throw" => Some(GestureType::Throw),
            "pinch" => Some(GestureType::Pinch),
            "peace_sign" => Some(GestureType::PeaceSign),
            "thumb_up" => Some(GestureType::ThumbUp),
            "thumb_down" => Some(GestureType::ThumbDown),
            "wave" => Some(GestureType::Wave),
            "circle" => Some(GestureType::Circle),
            "fist" => Some(GestureType::Fist),
            "two_fingers" => Some(GestureType::TwoFingers),
            "open_palm" => Some(GestureType::OpenPalm),
            "custom" => Some(GestureType::Custom),
            _ => None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GestureAction {
    pub gesture: GestureType,
    pub action: GestureActionKind,
    pub confidence: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum GestureActionKind {
    SendFile,
    ReceiveFile,
    Cancel,
    NextDevice,
    Preview,
    Pause,
    MultiSelect,
    Settings,
    Connect,
    Copy,
    Paste,
    Accept,
    Reject,
}

impl GestureActionKind {
    pub fn default_for_gesture(gesture: GestureType) -> Self {
        match gesture {
            GestureType::Throw => GestureActionKind::SendFile,
            GestureType::Grab => GestureActionKind::ReceiveFile,
            GestureType::SwipeLeft => GestureActionKind::Cancel,
            GestureType::SwipeRight => GestureActionKind::SendFile,
            GestureType::Pinch => GestureActionKind::Copy,
            GestureType::OpenPalm => GestureActionKind::Paste,
            GestureType::Wave => GestureActionKind::Connect,
            GestureType::TwoFingers => GestureActionKind::NextDevice,
            GestureType::Circle => GestureActionKind::Settings,
            GestureType::Fist => GestureActionKind::Pause,
            GestureType::ThumbUp => GestureActionKind::Accept,
            GestureType::ThumbDown => GestureActionKind::Reject,
            GestureType::Palm => GestureActionKind::Preview,
            GestureType::PeaceSign => GestureActionKind::MultiSelect,
            _ => GestureActionKind::SendFile,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HandLandmark {
    pub x: f32,
    pub y: f32,
    pub z: f32,
    pub visibility: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HandPose {
    pub landmarks: Vec<HandLandmark>,
    pub hand_side: HandSide,
    pub confidence: f32,
    pub timestamp: u64,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub enum HandSide {
    Left,
    Right,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GestureFrame {
    pub pose: HandPose,
    pub velocity: f32,
    pub acceleration: f32,
    pub direction: Direction3D,
    pub rotation: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Direction3D {
    pub x: f32,
    pub y: f32,
    pub z: f32,
}

impl Direction3D {
    pub const ZERO: Self = Direction3D {
        x: 0.0,
        y: 0.0,
        z: 0.0,
    };

    pub fn magnitude(&self) -> f32 {
        (self.x * self.x + self.y * self.y + self.z * self.z).sqrt()
    }

    pub fn normalized(&self) -> Self {
        let mag = self.magnitude();
        if mag < 0.001 {
            Self::ZERO
        } else {
            Direction3D {
                x: self.x / mag,
                y: self.y / mag,
                z: self.z / mag,
            }
        }
    }

    pub fn angle_to(&self, other: &Self) -> f32 {
        let dot = self.x * other.x + self.y * other.y + self.z * other.z;
        let mag_product = self.magnitude() * other.magnitude();
        if mag_product < 0.001 {
            180.0
        } else {
            let cos_angle = (dot / mag_product).clamp(-1.0, 1.0);
            cos_angle.acos().to_degrees()
        }
    }
}

pub fn classify_gesture_from_landmarks(landmarks: &[HandLandmark]) -> Option<(GestureType, f32)> {
    if landmarks.len() != 21 {
        return None;
    }

    let wrist = &landmarks[0];
    let thumb_tip = &landmarks[4];
    let index_tip = &landmarks[8];
    let middle_tip = &landmarks[12];
    let ring_tip = &landmarks[16];
    let pinky_tip = &landmarks[20];
    let index_mcp = &landmarks[5];
    let pinky_mcp = &landmarks[17];

    let thumb_open = distance(wrist, thumb_tip) > distance(wrist, index_mcp) * 0.6;
    let index_open = is_finger_extended(landmarks, 8, 5, 6, 7);
    let middle_open = is_finger_extended(landmarks, 12, 9, 10, 11);
    let ring_open = is_finger_extended(landmarks, 16, 13, 14, 15);
    let pinky_open = is_finger_extended(landmarks, 20, 17, 18, 19);

    let fingers_extended = [index_open, middle_open, ring_open, pinky_open]
        .iter()
        .filter(|&&x| x)
        .count();

    let gesture = match (thumb_open, fingers_extended) {
        (false, 0) => GestureType::Grab,
        (true, 4) => GestureType::OpenHand,
        (false, 4) => GestureType::Palm,
        (false, 1) if index_open => GestureType::Point,
        (false, 2) if index_open && middle_open => GestureType::PeaceSign,
        (true, 0) => {
            if thumb_tip.y < wrist.y {
                GestureType::ThumbUp
            } else {
                GestureType::ThumbDown
            }
        }
        (true, 3..=4) => GestureType::Throw,
        (false, 0) => GestureType::Fist,
        _ => GestureType::OpenHand,
    };

    let confidence = compute_confidence(landmarks, &gesture);
    Some((gesture, confidence))
}

fn is_finger_extended(
    landmarks: &[HandLandmark],
    tip_idx: usize,
    mcp_idx: usize,
    pip_idx: usize,
    dip_idx: usize,
) -> bool {
    let wrist = &landmarks[0];
    let tip_dist = distance(wrist, &landmarks[tip_idx]);
    let mcp_dist = distance(wrist, &landmarks[mcp_idx]);
    tip_dist > mcp_dist * 1.2
}

fn distance(a: &HandLandmark, b: &HandLandmark) -> f32 {
    let dx = a.x - b.x;
    let dy = a.y - b.y;
    let dz = a.z - b.z;
    (dx * dx + dy * dy + dz * dz).sqrt()
}

fn compute_confidence(landmarks: &[HandLandmark], _gesture: &GestureType) -> f32 {
    let avg_visibility: f32 =
        landmarks.iter().map(|l| l.visibility).sum::<f32>() / landmarks.len() as f32;
    let spatial_variance = {
        let wrist = &landmarks[0];
        landmarks
            .iter()
            .map(|l| {
                let dx = l.x - wrist.x;
                let dy = l.y - wrist.y;
                dx * dx + dy * dy
            })
            .sum::<f32>()
            / landmarks.len() as f32
    };
    let spatial_confidence = 1.0 - (spatial_variance / 2.0).clamp(0.0, 0.3);
    (avg_visibility * 0.7 + spatial_confidence * 0.3).clamp(0.0, 1.0)
}
