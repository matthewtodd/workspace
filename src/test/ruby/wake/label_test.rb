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

  def test_workspace_path
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name', label.path
  end

  def test_workspace_path_with_suffix
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name.suffix', label.path('suffix')
  end

  def test_external_path
    skip 'Not yet sure what to do here.'
    label = Wake::Label.parse('@repository//package:name')
    assert_equal 'lib/repository/package/name', label.path
  end
end
