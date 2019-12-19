require 'rubygems'
require 'minitest'
require 'wake/label'

class LabelTest < Minitest::Test
  def test_parse_repository
    label = Wake::Label.parse('//package:name')
    assert_nil label.repository
  end

  def test_parse_repository_external
    label = Wake::Label.parse('@repository//package:name')
    assert_equal 'repository', label.repository
  end

  def test_parse_package
    label = Wake::Label.parse('//package:name')
    assert_equal 'package', label.package
  end

  def test_parse_package_blank
    label = Wake::Label.parse('//:name')
    assert_equal '', label.package
  end

  def test_parse_package_nested
    label = Wake::Label.parse('//path/to/package:name')
    assert_equal 'path/to/package', label.package
  end

  def test_parse_name
    label = Wake::Label.parse('//package:name')
    assert_equal 'name', label.name
  end

  def test_parse_path
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name', label.path
  end

  def test_parse_path_with_suffix
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name.suffix', label.path('suffix')
  end

  def test_parse_path_external
    label = Wake::Label.parse('@repository//package:name')
    assert_equal 'external/repository/package/name', label.path
  end
end
