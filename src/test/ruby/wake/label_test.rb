require 'rubygems'
require 'minitest'
require 'wake/label'

class LabelTest < Minitest::Test
  def test_parse_workspace_repository
    label = Wake::Label.parse('//package:name')
    assert_nil label.repository
  end

  def test_parse_external_repository
    label = Wake::Label.parse('@repository//package:name')
    assert_equal 'repository', label.repository
  end

  def test_parse_package
    label = Wake::Label.parse('//package:name')
    assert_equal 'package', label.package
  end

  def test_parse_deep_package
    label = Wake::Label.parse('//path/to/package:name')
    assert_equal 'path/to/package', label.package
  end

  def test_parse_name
    label = Wake::Label.parse('//package:name')
    assert_equal 'name', label.name
  end
end
