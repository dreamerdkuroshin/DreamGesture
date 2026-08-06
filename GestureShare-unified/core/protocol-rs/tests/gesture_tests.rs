use gesture_protocol::gesture::*;

#[test]
fn test_gesture_type_as_str() {
    assert_eq!(GestureType::Throw.as_str(), "throw");
    assert_eq!(GestureType::Grab.as_str(), "grab");
    assert_eq!(GestureType::Palm.as_str(), "palm");
}

#[test]
fn test_gesture_type_from_str() {
    assert_eq!(GestureType::from_str("throw"), Some(GestureType::Throw));
    assert_eq!(GestureType::from_str("grab"), Some(GestureType::Grab));
    assert_eq!(GestureType::from_str("invalid"), None);
}

#[test]
fn test_direction3d_magnitude() {
    let dir = Direction3D { x: 3.0, y: 4.0, z: 0.0 };
    assert!((dir.magnitude() - 5.0).abs() < 0.01);
}

#[test]
fn test_direction3d_normalized() {
    let dir = Direction3D { x: 3.0, y: 4.0, z: 0.0 };
    let normalized = dir.normalized();
    assert!((normalized.magnitude() - 1.0).abs() < 0.01);
}

#[test]
fn test_direction3d_angle() {
    let dir_a = Direction3D { x: 1.0, y: 0.0, z: 0.0 };
    let dir_b = Direction3D { x: 0.0, y: 1.0, z: 0.0 };
    let angle = dir_a.angle_to(&dir_b);
    assert!((angle - 90.0).abs() < 0.01);
}

#[test]
fn test_gesture_action_default() {
    let action = GestureActionKind::default_for_gesture(GestureType::Throw);
    assert_eq!(action, GestureActionKind::SendFile);

    let action = GestureActionKind::default_for_gesture(GestureType::Grab);
    assert_eq!(action, GestureActionKind::ReceiveFile);
}

#[test]
fn test_gesture_classification_grab() {
    let landmarks = create_fist_landmarks();
    let result = classify_gesture_from_landmarks(&landmarks);
    assert!(result.is_some());
    let (gesture, confidence) = result.unwrap();
    assert_eq!(gesture, GestureType::Grab);
    assert!(confidence > 0.5);
}

#[test]
fn test_gesture_classification_open_hand() {
    let landmarks = create_open_hand_landmarks();
    let result = classify_gesture_from_landmarks(&landmarks);
    assert!(result.is_some());
    let (gesture, _) = result.unwrap();
    assert!(matches!(gesture, GestureType::OpenHand | GestureType::Palm));
}

#[test]
fn test_invalid_landmark_count() {
    let landmarks = vec![HandLandmark { x: 0.5, y: 0.5, z: 0.0, visibility: 1.0 }; 10];
    let result = classify_gesture_from_landmarks(&landmarks);
    assert!(result.is_none());
}

fn create_fist_landmarks() -> Vec<HandLandmark> {
    let mut landmarks = Vec::with_capacity(21);
    landmarks.push(HandLandmark { x: 0.5, y: 0.5, z: 0.0, visibility: 1.0 });
    for i in 1..21 {
        let t = i as f32 / 21.0;
        landmarks.push(HandLandmark { x: 0.5 + t * 0.05, y: 0.5 + t * 0.05, z: 0.0, visibility: 1.0 });
    }
    landmarks
}

fn create_open_hand_landmarks() -> Vec<HandLandmark> {
    let mut landmarks = Vec::with_capacity(21);
    landmarks.push(HandLandmark { x: 0.5, y: 0.8, z: 0.0, visibility: 1.0 });
    let finger_tips = [(0.3, 0.3), (0.4, 0.2), (0.5, 0.15), (0.6, 0.2), (0.7, 0.3)];
    for (i, (tip_x, tip_y)) in finger_tips.iter().enumerate() {
        let base_idx = i * 4 + 1;
        landmarks.push(HandLandmark { x: *tip_x, y: *tip_y, z: 0.0, visibility: 1.0 });
        landmarks.push(HandLandmark { x: *tip_x, y: *tip_y + 0.05, z: 0.0, visibility: 1.0 });
        landmarks.push(HandLandmark { x: *tip_x, y: *tip_y + 0.1, z: 0.0, visibility: 1.0 });
        landmarks.push(HandLandmark { x: *tip_x, y: *tip_y + 0.15, z: 0.0, visibility: 1.0 });
    }
    landmarks
}
