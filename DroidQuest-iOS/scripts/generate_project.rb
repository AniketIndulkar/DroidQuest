#!/usr/bin/env ruby
# frozen_string_literal: true

require 'xcodeproj'

root = File.expand_path('..', __dir__)
project_path = File.join(root, 'DroidQuest.xcodeproj')
project = Xcodeproj::Project.new(project_path)
project.root_object.attributes['LastSwiftUpdateCheck'] = '2640'
project.root_object.attributes['LastUpgradeCheck'] = '2640'

app = project.new_target(:application, 'DroidQuest', :ios, '17.0')
tests = project.new_target(:unit_test_bundle, 'DroidQuestTests', :ios, '17.0')
tests.add_dependency(app)

app_group = project.main_group.new_group('DroidQuest', 'DroidQuest')
Dir.glob(File.join(root, 'DroidQuest', '**', '*.swift')).sort.each do |path|
  ref = app_group.new_file(path.sub("#{root}/DroidQuest/", ''))
  app.source_build_phase.add_file_reference(ref)
end

tests_group = project.main_group.new_group('DroidQuestTests', 'DroidQuestTests')
Dir.glob(File.join(root, 'DroidQuestTests', '**', '*.swift')).sort.each do |path|
  ref = tests_group.new_file(path.sub("#{root}/DroidQuestTests/", ''))
  tests.source_build_phase.add_file_reference(ref)
end

# Keep data/content as the single source of truth. Xcode copies it into the app bundle
# as a folder resource, preserving the same content/... paths used by Android.
content = project.main_group.new_reference('../data/content', 'SOURCE_ROOT')
content.name = 'Shared Curriculum (data/content)'
content.last_known_file_type = 'folder'
app.resources_build_phase.add_file_reference(content)

project.build_configurations.each do |config|
  config.build_settings['SWIFT_VERSION'] = '6.0'
  config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '17.0'
end

app.build_configurations.each do |config|
  config.build_settings.merge!(
    'PRODUCT_BUNDLE_IDENTIFIER' => 'dev.novanest.droidquest',
    'PRODUCT_NAME' => 'DroidQuest',
    'GENERATE_INFOPLIST_FILE' => 'YES',
    'INFOPLIST_KEY_CFBundleDisplayName' => 'DroidQuest',
    'INFOPLIST_KEY_LSApplicationCategoryType' => 'public.app-category.education',
    'INFOPLIST_KEY_UIApplicationSceneManifest_Generation' => 'YES',
    'INFOPLIST_KEY_UILaunchScreen_Generation' => 'YES',
    'INFOPLIST_KEY_UISupportedInterfaceOrientations_iPhone' => 'UIInterfaceOrientationPortrait',
    'MARKETING_VERSION' => '0.2.0',
    'CURRENT_PROJECT_VERSION' => '2',
    'TARGETED_DEVICE_FAMILY' => '1,2',
    'CODE_SIGN_STYLE' => 'Automatic',
    'DEVELOPMENT_TEAM' => '',
    'SWIFT_EMIT_LOC_STRINGS' => 'YES'
  )
end

tests.build_configurations.each do |config|
  config.build_settings.merge!(
    'PRODUCT_BUNDLE_IDENTIFIER' => 'dev.novanest.droidquest.tests',
    'GENERATE_INFOPLIST_FILE' => 'YES',
    'TEST_HOST' => '$(BUILT_PRODUCTS_DIR)/DroidQuest.app/$(BUNDLE_EXECUTABLE_FOLDER_PATH)/DroidQuest',
    'BUNDLE_LOADER' => '$(TEST_HOST)',
    'SWIFT_VERSION' => '6.0'
  )
end

project.save
